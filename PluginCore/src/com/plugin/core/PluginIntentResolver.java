package com.plugin.core;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.plugin.content.PluginActivityInfo;
import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginReceiverIntent;
import com.plugin.core.stub.PluginStubReceiver;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

public class PluginIntentResolver {

	static final String ACTIVITY_ACTION_IN_PLUGIN = "_ACTIVITY_ACTION_IN_PLUGIN_";

	static final String prefix = "plugin_receiver_or_service_prefix.";

	private static String RECEIVER_ACTION_IN_PLUGIN = "_RECEIVER_ACTION_IN_PLUGIN_";

	/* package */static void resolveService(Intent service) {
		String className = PluginLoader.matchPlugin(service);
		if (className != null) {
			ClassLoaderUtil.hackHostClassLoaderIfNeeded();
			String stubActivityName = PluginStubBinding.bindStubService(className);
			if (stubActivityName != null) {
				service.setClassName(PluginLoader.getApplicatoin(), stubActivityName);
			}
		}
	}

	/* package */static Intent resolveReceiver(final Intent intent) {
		// 如果在插件中发现了匹配intent的receiver项目，替换掉ClassLoader
		// 不需要在这里记录目标className，className将在Intent中传递
		String className = PluginLoader.matchPlugin(intent);
		if (className != null) {
			ClassLoaderUtil.hackHostClassLoaderIfNeeded();
			intent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(),
					PluginStubReceiver.class.getName()));
			//hackReceiverForClassLoader检测到这个标记后会进行替换
			intent.setAction(className + RECEIVER_ACTION_IN_PLUGIN + (intent.getAction() == null ? "" : intent.getAction()));
		}
		return intent;
	}

	/* package */static Class hackReceiverForClassLoader(Object msgObj) {
		Intent intent = (Intent) RefInvoker.getFieldObject(msgObj, "android.app.ActivityThread$ReceiverData", "intent");
		if (intent.getComponent().getClassName().equals(PluginStubReceiver.class.getName())) {
			String action = intent.getAction();
			LogUtil.d("action", action);
			if (action != null) {
				String[] targetClassName = action.split(RECEIVER_ACTION_IN_PLUGIN);
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
						prefix + targetClassName[0]));

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
			info.name =  prefix + targetClassName;
		} else {
			LogUtil.d("hackServiceName 没有找到映射关系, 说明是宿主service（也可能是映射表出了异常）", info.name);
		}
		return targetClassName;
	}

	/* package */static void resolveActivity(Intent intent) {
		// 如果在插件中发现Intent的匹配项，记下匹配的插件Activity的ClassName
		String className = PluginLoader.matchPlugin(intent);
		if (className != null) {

			PluginDescriptor pd = PluginLoader.getPluginDescriptorByClassName(className);

			PluginActivityInfo pluginActivityInfo = pd.getActivityInfos().get(className);

			String stubActivityName = PluginStubBinding.bindLaunchModeStubActivity(className, Integer.parseInt(pluginActivityInfo.getLaunchMode()));

			intent.setComponent(
					new ComponentName(PluginLoader.getApplicatoin().getPackageName(), stubActivityName));
			//PluginInstrumentationWrapper检测到这个标记后会进行替换
			intent.setAction(className + ACTIVITY_ACTION_IN_PLUGIN + (intent.getAction()==null?"":intent.getAction()));
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
	public static Intent resolveNotificationIntent(Intent intent) {
		int type = PluginLoader.getTargetType(intent);

		LogUtil.d("notification type", type);

		if (type == PluginDescriptor.BROADCAST) {

			Intent newIntent = PluginIntentResolver.resolveReceiver(intent);
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
	public static PendingIntent resolvePendingIntent(PendingIntent origin) {
		if (origin != null) {
			Intent originIntent = (Intent)RefInvoker.invokeMethod(origin,
					PendingIntent.class.getName(), "getIntent",
					(Class[])null, (Object[])null);
			if (originIntent != null) {
				//如果目标是插件中的组件，需要额外提供2个参数, 默认为0、Update_Current。
				String className = PluginLoader.matchPlugin(originIntent);
				if (className != null) {

					int type = PluginLoader.getTargetType(originIntent);

					int requestCode = originIntent.getIntExtra("pending_requestCode", 0);
					int flags = originIntent.getIntExtra("pending_flag", PendingIntent.FLAG_UPDATE_CURRENT);

					if (type == PluginDescriptor.BROADCAST) {

						Intent newIntent = PluginIntentResolver.resolveReceiver(originIntent);
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
