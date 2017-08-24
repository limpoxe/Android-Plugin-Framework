package com.limpoxe.fairy.core;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.android.HackActivityThread;
import com.limpoxe.fairy.core.android.HackContextImpl;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用于拦截Activity  Service  Receiver的生命周期函数
 * 同时可以跟踪一些其他消息
 * @author cailiming
 * 
 */
public class PluginAppTrace implements Handler.Callback {

	private final Handler mHandler;

	public PluginAppTrace(Handler handler) {
		mHandler = handler;
	}

	@Override
	public boolean handleMessage(Message msg) {

		LogUtil.v(">>> handling: ", CodeConst.codeToString(msg.what));

		Result result = beforeHandle(msg);

		try {

			mHandler.handleMessage(msg);

			LogUtil.v(">>> done: " + CodeConst.codeToString(msg.what));

		} finally {

			afterHandle(msg, result);
			
		}

		return true;
	}

	private Result beforeHandle(Message msg) {

		switch (msg.what) {

			case CodeConst.LAUNCH_ACTIVITY:
			case CodeConst.RELAUNCH_ACTIVITY:

				beforeLaunchActivityFor360Safe();

				return null;

			case CodeConst.RECEIVER:

				return beforeReceiver(msg);

			case CodeConst.CREATE_SERVICE:

				return beforeCreateService(msg);

			case CodeConst.STOP_SERVICE:

				return beforeStopService(msg);
		}
		return null;
	}

	private static void beforeLaunchActivityFor360Safe() {
		// 检查mInstrumention是否已经替换成功。
		// 之所以要检查，是因为如果手机上安装了360手机卫士等app，它们可能会劫持用户app的ActivityThread对象，
		// 导致在PluginApplication的onCreate方法里面替换mInstrumention可能会失败
		// 所以这里再做一次检查
		PluginInjector.injectInstrumentation();
	}

	private static Result beforeReceiver(Message msg) {
		if (ProcessUtil.isPluginProcess()) {//判断进程是为了提高效率, 因为插件组件都是在插件进程中运行的.

            Context newBase = PluginIntentResolver.resolveReceiverForClassLoader(msg.obj);
			//找到class说明是插件中定义的receiver
			if (newBase != null) {

				Context baseContext = FairyGlobal.getHostApplication().getBaseContext();

				PluginInjector.replaceReceiverContext(baseContext, newBase);

				Result result = new Result();
				result.baseContext = baseContext;

				return result;
			} else {
				//宿主的Receiver的context不需要做特别处理，因为在framework中Receiver的context本身是对appliction的包装。
				//而宿主的application的baseContext已经在插件框架init的时候替换过了
			}
		}

		return null;
	}

	private static Result beforeCreateService(Message msg) {
		Result result = new Result();
		result.serviceName = PluginIntentResolver.resolveServiceForClassLoader(msg.obj);
		return result;
	}

	private static Result beforeStopService(Message msg) {
		if (ProcessUtil.isPluginProcess()) {
			//销毁service时回收映射关系, 之所以要回收映射关系是为了能在宿主中尽量少的注册占位组件.
			//即回收映射关系并不是必须的, 只要预注册的占位组件数据足够即可.
			if (HackActivityThread.get() != null) {
				Map<IBinder, Service> services = HackActivityThread.get().getServices();
				if (services != null) {
					Service service = services.get(msg.obj);
					if (service != null) {
						String pluginServiceClassName = service.getClass().getName();
						LogUtil.v("unBindStubService", pluginServiceClassName);
                        PluginProviderClient.unBindStubService(pluginServiceClassName);
					}
				}
			}
		}

		return null;
	}

	private static void afterHandle(Message msg, Result result) {
		switch (msg.what) {
			case  CodeConst.RECEIVER:

				afterReceiver(result);

				break;

			case CodeConst.CREATE_SERVICE:

				afterCreateService(result);

				break;

            case CodeConst.CONFIGURATION_CHANGED:

                afterConfigurationChanged(msg);

                break;
		}
	}

	private static void afterReceiver(Result result) {
		if (ProcessUtil.isPluginProcess()) {
			if (result != null && result.baseContext != null) {
				new HackContextImpl(result.baseContext).setReceiverRestrictedContext(null);
			}
		}
	}

	private static void afterCreateService(Result result) {
		//这里不做进程判断,是因为如果是宿主进程, 也需要为宿主service换context
		if (result.serviceName != null && result.serviceName.startsWith(PluginIntentResolver.CLASS_PREFIX_SERVICE)) {
			//替换service的context
			//在引入了PluginShadowService以后,这个已经是多余的了, 注释掉先.
			//PluginInjector.replacePluginServiceContext(result.serviceName.replace(PluginIntentResolver.CLASS_PREFIX_SERVICE, ""));
		} else {
			//给宿主service注入一个无害的BaseContext, 主要是为了重写宿主Service的sentBroadCast和startService方法
			//使得在宿主的service中通过intent可以打开插件的组件
			PluginInjector.replaceHostServiceContext(result.serviceName);
		}
	}

	private static void afterConfigurationChanged(Message msg) {
        if (ProcessUtil.isPluginProcess()) {
            ArrayList<PluginDescriptor> pluginDescriptors = PluginManagerHelper.getPlugins();
            for(PluginDescriptor pluginDescriptor : pluginDescriptors) {
                LoadedPlugin loadedPlugin = PluginLauncher.instance().getRunningPlugin(pluginDescriptor.getPackageName());
                if (loadedPlugin != null) {
                    //更新环境配置，如屏幕密度，系统语言，横竖屏等
                    //TODO updateConfiguration这个方法已经过期，后续需要更改为通过反射调用它的隐藏方法
                    LogUtil.v("updateConfiguration for ", pluginDescriptor.getPackageName());
                    loadedPlugin.pluginResource.updateConfiguration((Configuration)msg.obj, null);
                }
            }
        }
    }

	static class Result {
		String serviceName;
		Context baseContext;
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
		public static final int DUMP_PROVIDER           = 141;
		public static final int UNSTABLE_PROVIDER_DIED  = 142;
		public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
		public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
		public static final int INSTALL_PROVIDER        = 145;
		public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
		public static final int CANCEL_VISIBLE_BEHIND = 147;
		public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
		public static final int ENTER_ANIMATION_COMPLETE = 149;

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
			case DUMP_PROVIDER:
				return "DUMP_PROVIDER";
			case UNSTABLE_PROVIDER_DIED:
				return "UNSTABLE_PROVIDER_DIED";
			case REQUEST_ASSIST_CONTEXT_EXTRAS:
				return "REQUEST_ASSIST_CONTEXT_EXTRAS";
			case TRANSLUCENT_CONVERSION_COMPLETE:
				return "TRANSLUCENT_CONVERSION_COMPLETE";
			case INSTALL_PROVIDER:
				return "INSTALL_PROVIDER";
			case ON_NEW_ACTIVITY_OPTIONS:
				return "ON_NEW_ACTIVITY_OPTIONS";
			case CANCEL_VISIBLE_BEHIND:
				return "CANCEL_VISIBLE_BEHIND";
			case BACKGROUND_VISIBLE_BEHIND_CHANGED:
				return "BACKGROUND_VISIBLE_BEHIND_CHANGED";
			case ENTER_ANIMATION_COMPLETE:
				return "ENTER_ANIMATION_COMPLETE";
			}
			return "(unknown: " + code +")";
		}
	}

}