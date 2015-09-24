package com.plugin.core;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.proxy.PluginProxyService;
import com.plugin.core.ui.stub.PluginStubActivity;
import com.plugin.core.ui.stub.PluginStubReceiver;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

public class PluginIntentResolver {

	/* package */static void resolveService(Intent service) {
		String targetClassName = PluginLoader.isMatchPlugin(service);
		if (targetClassName != null) {
			service.setClass(PluginLoader.getApplicatoin(), PluginProxyService.class);
			service.putExtra(PluginProxyService.SERVICE_NAME, targetClassName);
		}
	}

	/* package */static Intent resolveReceiver(final Intent intent) {
		// 如果在插件中发现了匹配intent的receiver项目，替换掉ClassLoader
		// 不需要在这里记录目标className，className将在Intent中传递
		String className = PluginLoader.isMatchPlugin(intent);
		if (className != null) {
			ClassLoaderUtil.hackClassLoaderIfNeeded();
			intent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(),
					PluginStubReceiver.class.getName()));
			//hackReceiverForClassLoader检测到这个标记后会进行替换
			intent.setAction(className + PluginInstrumentionWrapper.ACTIVITY_ACTION_IN_PLUGIN + (intent.getAction() == null ? "" : intent.getAction()));
		}
		return intent;
	}

	/* package */static void hackReceiverForClassLoader(Object msgObj) {
		Intent intent = (Intent) RefInvoker.getFieldObject(msgObj, "android.app.ActivityThread$ReceiverData", "intent");
		if (intent.getComponent().getClassName().equals(PluginStubReceiver.class.getName())) {
			String action = intent.getAction();
			LogUtil.d("action", action);
			if (action != null) {
				String[] targetClassName = action.split(PluginInstrumentionWrapper.ACTIVITY_ACTION_IN_PLUGIN);
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
				intent.setComponent(new ComponentName(intent.getComponent().getPackageName(), PluginStubReceiver.class
						.getName() + "." + targetClassName[0]));
			}
		}
	}

	/* package */static boolean resolveStopService(final Intent name) {
		if (PluginLoader.isMatchPlugin(name) != null) {
			resolveService(name);
			name.putExtra(PluginProxyService.DESTORY_SERVICE, true);
			return true;
		}
		return false;
	}

	/* package */static void resolveActivity(Intent intent) {
		// 如果在插件中发现Intent的匹配项，记下匹配的插件Activity的ClassName
		String className = PluginLoader.isMatchPlugin(intent);
		if (className != null) {
			intent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(),
					PluginStubActivity.class.getName()));
			//PluginInstrumentationWrapper检测到这个标记后会进行替换
			intent.setAction(className + PluginInstrumentionWrapper.ACTIVITY_ACTION_IN_PLUGIN + (intent.getAction()==null?"":intent.getAction()));
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
				String className = PluginLoader.isMatchPlugin(originIntent);
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
