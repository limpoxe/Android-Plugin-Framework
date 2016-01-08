package com.plugin.core;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.plugin.content.PluginActivityInfo;
import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginReceiverIntent;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.util.ArrayList;

public class PluginIntentResolver {

	static final String CLASS_SEPARATOR = "_RECEIVER_AND_ACTIVITY_";
	static final String CLASS_PREFIX = "_RECEIVER_AND_SERVICE_";

	/* package */static void resolveService(Intent service) {
		ArrayList<String> classNameList = PluginLoader.matchPlugin(service, PluginDescriptor.SERVICE);
		if (classNameList != null && classNameList.size() > 0) {
			ClassLoaderUtil.hackHostClassLoaderIfNeeded();
			String stubServiceName = PluginStubBinding.bindStubService(classNameList.get(0));
			if (stubServiceName != null) {
				service.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(), stubServiceName));
			}
		}
	}

	/* package */static ArrayList<Intent> resolveReceiver(final Intent intent) {
		// 如果在插件中发现了匹配intent的receiver项目，替换掉ClassLoader
		// 不需要在这里记录目标className，className将在Intent中传递
		ArrayList<Intent> result = new ArrayList<Intent>();
		ArrayList<String> classNameList = PluginLoader.matchPlugin(intent, PluginDescriptor.BROADCAST);
		if (classNameList != null) {
			ClassLoaderUtil.hackHostClassLoaderIfNeeded();

			for(String className: classNameList) {
				Intent newIntent = new Intent(intent);
				newIntent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(),
						PluginStubBinding.bindStubReceiver()));
				//hackReceiverForClassLoader检测到这个标记后会进行替换
				newIntent.setAction(className + CLASS_SEPARATOR + (intent.getAction() == null ? "" : intent.getAction()));
				result.add(newIntent);
			}
		} else {
			result.add(intent);
		}
		return result;
	}

	/* package */static Class hackReceiverForClassLoader(Object msgObj) {
		Intent intent = (Intent) RefInvoker.getFieldObject(msgObj, "android.app.ActivityThread$ReceiverData", "intent");
		if (intent.getComponent().getClassName().equals(PluginStubBinding.bindStubReceiver())) {
			String action = intent.getAction();
			LogUtil.d("action", action);
			if (action != null) {
				String[] targetClassName = action.split(CLASS_SEPARATOR);
				@SuppressWarnings("rawtypes")
				Class clazz = PluginLoader.loadPluginClassByName(targetClassName[0]);
				if (clazz != null) {
					intent.setExtrasClassLoader(clazz.getClassLoader());
					//由于之前intent被修改过 这里再吧Intent还原到原始的intent
					if (targetClassName.length > 1) {
						intent.setAction(targetClassName[1]);
					} else {
						intent.setAction(null);
					}
				}
				// PluginClassLoader检测到这个特殊标记后会进行替换
				intent.setComponent(new ComponentName(intent.getComponent().getPackageName(),
						CLASS_PREFIX + targetClassName[0]));

				if (Build.VERSION.SDK_INT >= 21) {
					if (intent.getExtras() != null) {
						PluginReceiverIntent newIntent = new PluginReceiverIntent(intent);
						RefInvoker.setFieldObject(msgObj, "android.app.ActivityThread$ReceiverData", "intent", newIntent);
					}
				}

				return clazz;
			}
		}
		return null;
	}

	/* package */static String hackServiceName(Object msgObj) {
		ServiceInfo info = (ServiceInfo) RefInvoker.getFieldObject(msgObj, "android.app.ActivityThread$CreateServiceData", "info");
		//通过映射查找
		String targetClassName = PluginStubBinding.getBindedPluginServiceName(info.name);

		LogUtil.d("hackServiceName", info.name, info.packageName, info.processName, "targetClassName", targetClassName);

		if (targetClassName != null) {
			info.name =  CLASS_PREFIX + targetClassName;
		} else {
			LogUtil.e("hackServiceName 没有找到映射关系, 说明是宿主service（也可能是映射表出了异常）", info.name);
		}
		return targetClassName;
	}

	/* package */static void resolveActivity(Intent intent) {
		// 如果在插件中发现Intent的匹配项，记下匹配的插件Activity的ClassName
		ArrayList<String> classNameList = PluginLoader.matchPlugin(intent, PluginDescriptor.ACTIVITY);
		if (classNameList != null && classNameList.size() >0) {

			String className = classNameList.get(0);
			PluginDescriptor pd = PluginLoader.getPluginDescriptorByClassName(className);

			PluginActivityInfo pluginActivityInfo = pd.getActivityInfos().get(className);

			String stubActivityName = PluginStubBinding.bindStubActivity(className, Integer.parseInt(pluginActivityInfo.getLaunchMode()));

			intent.setComponent(
					new ComponentName(PluginLoader.getApplicatoin().getPackageName(), stubActivityName));
			//PluginInstrumentationWrapper检测到这个标记后会进行替换
			intent.setAction(className + CLASS_SEPARATOR + (intent.getAction()==null?"":intent.getAction()));
		}
	}

	/* package */static void resolveActivity(Intent[] intent) {
		// not needed
	}

	/**
	 * used before send notification
	 * @param intent
	 * @return
	 */
	public static Intent resolveNotificationIntent(Intent intent, int type) {

		if (type == PluginDescriptor.BROADCAST) {

			Intent newIntent = PluginIntentResolver.resolveReceiver(intent).get(0);
			return newIntent;

		} else if (type == PluginDescriptor.ACTIVITY) {

			PluginIntentResolver.resolveActivity(intent);
			return intent;

		} else if (type == PluginDescriptor.SERVICE) {

			PluginIntentResolver.resolveService(intent);
			return intent;

		}
		return intent;
	}

	@SuppressWarnings("ResourceType")
	public static PendingIntent resolvePendingIntent(PendingIntent origin, int type) {
		if (origin != null) {
			Intent originIntent = (Intent)RefInvoker.invokeMethod(origin,
					PendingIntent.class.getName(), "getIntent",
					(Class[])null, (Object[])null);
			if (originIntent != null) {
				//如果目标是插件中的组件，需要额外提供2个参数, 默认为0、Update_Current。
				ArrayList<String> classNameList = PluginLoader.matchPlugin(originIntent, type);
				if (classNameList != null && classNameList.size() > 0) {

					int requestCode = originIntent.getIntExtra("pending_requestCode", 0);
					int flags = originIntent.getIntExtra("pending_flag", PendingIntent.FLAG_UPDATE_CURRENT);

					if (type == PluginDescriptor.BROADCAST) {

						Intent newIntent = PluginIntentResolver.resolveReceiver(originIntent).get(0);
						return PendingIntent.getBroadcast(PluginLoader.getApplicatoin(), requestCode, newIntent, flags);

					} else if (type == PluginDescriptor.ACTIVITY) {

						PluginIntentResolver.resolveActivity(originIntent);
						return PendingIntent.getActivity(PluginLoader.getApplicatoin(), requestCode, originIntent, flags);

					} else if (type == PluginDescriptor.SERVICE) {

						PluginIntentResolver.resolveService(originIntent);
						return PendingIntent.getService(PluginLoader.getApplicatoin(), requestCode, originIntent, flags);

					}
				}
			}
		}
		return origin;
	}
}
