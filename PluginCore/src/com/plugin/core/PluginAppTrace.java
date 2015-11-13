package com.plugin.core;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.util.Iterator;
import java.util.Map;

/**
 * 插件Receiver免注册的主要实现原理
 * 
 * @author cailiming
 * 
 */
public class PluginAppTrace implements Handler.Callback {

	private final Handler mHandler;

	protected PluginAppTrace(Handler handler) {
		mHandler = handler;
	}

	@Override
	public boolean handleMessage(Message msg) {

		LogUtil.d(">>> handling: ", CodeConst.codeToString(msg.what));

		String serviceName = null;
		Context baseContext = null;

		if (msg.what == CodeConst.RECEIVER) {

			Class clazz = PluginIntentResolver.hackReceiverForClassLoader(msg.obj);

			baseContext = replaceReceiverContext(clazz);

		} else if (msg.what == CodeConst.CREATE_SERVICE) {

			serviceName = PluginIntentResolver.hackServiceName(msg.obj);

			if (serviceName != null) {
				ClassLoaderUtil.hackClassLoaderIfNeeded();
			}
		} else if (msg.what == CodeConst.STOP_SERVICE) {
			//销毁service时回收映射关系
			Object activityThread = PluginInjector.getActivityThread();
			if (activityThread != null) {
				Map<IBinder, Service> services = (Map<IBinder, Service>)RefInvoker.getFieldObject(activityThread, "android.app.ActivityThread", "mServices");
				if (services != null) {
					Service service = services.get(msg.obj);
					if (service != null) {
						String pluginServiceClassName = service.getClass().getName();
						LogUtil.d("STOP_SERVICE", pluginServiceClassName);
						PluginStubBinding.unBindStubService(pluginServiceClassName);
					}
				}
			}
		}

		try {
			mHandler.handleMessage(msg);
			LogUtil.d(">>> done: " + CodeConst.codeToString(msg.what));
		} finally {
			if (msg.what == CodeConst.RECEIVER && baseContext != null) {

				RefInvoker.setFieldObject(baseContext, "android.app.ContextImpl", "mReceiverRestrictedContext", null);

			} else if (msg.what == CodeConst.CREATE_SERVICE) {
				//拿到创建好的service，重新 设置mBase和mApplicaiton
				//由于这步操作是再service得oncreate之后执行，所以再插件service得oncreate中不应尝试通过此service的context执行操作
				replaceServiceContext(serviceName);
			}
		}

		return true;
	}

	private static Context replaceReceiverContext(Class clazz) {
		if (clazz == null) {
			return null;
		}
		Context baseContext = PluginLoader.getApplicatoin().getBaseContext();
		if (baseContext.getClass().getName().equals("android.app.ContextImpl")) {
			ContextWrapper receiverRestrictedContext = (ContextWrapper) RefInvoker.invokeMethod(baseContext, "android.app.ContextImpl", "getReceiverRestrictedContext", (Class[]) null, (Object[]) null);
			RefInvoker.setFieldObject(receiverRestrictedContext, ContextWrapper.class.getName(), "mBase", PluginLoader.getDefaultPluginContext(clazz));
		} else {
			baseContext = null;
		}
		return baseContext;
	}

	private static void replaceServiceContext(String serviceName) {
		Object activityThread = PluginInjector.getActivityThread();
		if (activityThread != null) {
			Map<IBinder, Service> services = (Map<IBinder, Service>)RefInvoker.getFieldObject(activityThread, "android.app.ActivityThread", "mServices");
			if (services != null) {
				Iterator<Service> itr = services.values().iterator();
				while(itr.hasNext()) {
					Service service = itr.next();
					if (service != null && service.getClass().getName().equals(serviceName) ) {

						PluginDescriptor pd = PluginLoader.getPluginDescriptorByClassName(serviceName);

						RefInvoker.setFieldObject(service, ContextWrapper.class.getName(), "mBase", PluginLoader.getNewPluginContext(pd.getPluginContext()));

						if (pd.getPluginApplication() != null) {
							RefInvoker.setFieldObject(service, Service.class.getName(), "mApplication", pd.getPluginApplication());
						}
					}

				}
			}

		}
	}

	private static class CodeConst {
		public static final int LAUNCH_ACTIVITY = 100;
		public static final int PAUSE_ACTIVITY = 101;
		public static final int PAUSE_ACTIVITY_FINISHING = 102;
		public static final int STOP_ACTIVITY_SHOW = 103;
		public static final int STOP_ACTIVITY_HIDE = 104;
		public static final int SHOW_WINDOW = 105;
		public static final int HIDE_WINDOW = 106;
		public static final int RESUME_ACTIVITY = 107;
		public static final int SEND_RESULT = 108;
		public static final int DESTROY_ACTIVITY = 109;
		public static final int BIND_APPLICATION = 110;
		public static final int EXIT_APPLICATION = 111;
		public static final int NEW_INTENT = 112;
		public static final int RECEIVER = 113;
		public static final int CREATE_SERVICE = 114;
		public static final int SERVICE_ARGS = 115;
		public static final int STOP_SERVICE = 116;
		public static final int REQUEST_THUMBNAIL = 117;
		public static final int CONFIGURATION_CHANGED = 118;
		public static final int CLEAN_UP_CONTEXT = 119;
		public static final int GC_WHEN_IDLE = 120;
		public static final int BIND_SERVICE = 121;
		public static final int UNBIND_SERVICE = 122;
		public static final int DUMP_SERVICE = 123;
		public static final int LOW_MEMORY = 124;
		public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
		public static final int RELAUNCH_ACTIVITY = 126;
		public static final int PROFILER_CONTROL = 127;
		public static final int CREATE_BACKUP_AGENT = 128;
		public static final int DESTROY_BACKUP_AGENT = 129;
		public static final int SUICIDE = 130;
		public static final int REMOVE_PROVIDER = 131;
		public static final int ENABLE_JIT = 132;
		public static final int DISPATCH_PACKAGE_BROADCAST = 133;
		public static final int SCHEDULE_CRASH = 134;
		public static final int DUMP_HEAP = 135;
		public static final int DUMP_ACTIVITY = 136;
		public static final int SLEEPING = 137;
		public static final int SET_CORE_SETTINGS = 138;
		public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
		public static final int TRIM_MEMORY = 140;

		public static String codeToString(int code) {
			switch (code) {
			case LAUNCH_ACTIVITY:
				return "LAUNCH_ACTIVITY";
			case PAUSE_ACTIVITY:
				return "PAUSE_ACTIVITY";
			case PAUSE_ACTIVITY_FINISHING:
				return "PAUSE_ACTIVITY_FINISHING";
			case STOP_ACTIVITY_SHOW:
				return "STOP_ACTIVITY_SHOW";
			case STOP_ACTIVITY_HIDE:
				return "STOP_ACTIVITY_HIDE";
			case SHOW_WINDOW:
				return "SHOW_WINDOW";
			case HIDE_WINDOW:
				return "HIDE_WINDOW";
			case RESUME_ACTIVITY:
				return "RESUME_ACTIVITY";
			case SEND_RESULT:
				return "SEND_RESULT";
			case DESTROY_ACTIVITY:
				return "DESTROY_ACTIVITY";
			case BIND_APPLICATION:
				return "BIND_APPLICATION";
			case EXIT_APPLICATION:
				return "EXIT_APPLICATION";
			case NEW_INTENT:
				return "NEW_INTENT";
			case RECEIVER:
				return "RECEIVER";
			case CREATE_SERVICE:
				return "CREATE_SERVICE";
			case SERVICE_ARGS:
				return "SERVICE_ARGS";
			case STOP_SERVICE:
				return "STOP_SERVICE";
			case REQUEST_THUMBNAIL:
				return "REQUEST_THUMBNAIL";
			case CONFIGURATION_CHANGED:
				return "CONFIGURATION_CHANGED";
			case CLEAN_UP_CONTEXT:
				return "CLEAN_UP_CONTEXT";
			case GC_WHEN_IDLE:
				return "GC_WHEN_IDLE";
			case BIND_SERVICE:
				return "BIND_SERVICE";
			case UNBIND_SERVICE:
				return "UNBIND_SERVICE";
			case DUMP_SERVICE:
				return "DUMP_SERVICE";
			case LOW_MEMORY:
				return "LOW_MEMORY";
			case ACTIVITY_CONFIGURATION_CHANGED:
				return "ACTIVITY_CONFIGURATION_CHANGED";
			case RELAUNCH_ACTIVITY:
				return "RELAUNCH_ACTIVITY";
			case PROFILER_CONTROL:
				return "PROFILER_CONTROL";
			case CREATE_BACKUP_AGENT:
				return "CREATE_BACKUP_AGENT";
			case DESTROY_BACKUP_AGENT:
				return "DESTROY_BACKUP_AGENT";
			case SUICIDE:
				return "SUICIDE";
			case REMOVE_PROVIDER:
				return "REMOVE_PROVIDER";
			case ENABLE_JIT:
				return "ENABLE_JIT";
			case DISPATCH_PACKAGE_BROADCAST:
				return "DISPATCH_PACKAGE_BROADCAST";
			case SCHEDULE_CRASH:
				return "SCHEDULE_CRASH";
			case DUMP_HEAP:
				return "DUMP_HEAP";
			case DUMP_ACTIVITY:
				return "DUMP_ACTIVITY";
			case SLEEPING:
				return "SLEEPING";
			case SET_CORE_SETTINGS:
				return "SET_CORE_SETTINGS";
			case UPDATE_PACKAGE_COMPATIBILITY_INFO:
				return "UPDATE_PACKAGE_COMPATIBILITY_INFO";
			case TRIM_MEMORY:
				return "TRIM_MEMORY";
			}
			return "(unknown: " + code +")";
		}
	}

}