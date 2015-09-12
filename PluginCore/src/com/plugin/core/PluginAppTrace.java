package com.plugin.core;

import android.os.Handler;
import android.os.Message;

import com.plugin.util.LogUtil;

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
		try {
			LogUtil.d(">>> handling: ", CodeConst.codeToString(msg.what));
			if (msg.what == CodeConst.RECEIVER) {
				PluginIntentResolver.hackReceiverForClassLoader(msg.obj);
			}
			mHandler.handleMessage(msg);
			LogUtil.d(">>> done: " + CodeConst.codeToString(msg.what));
		} catch (Throwable e) {
			LogUtil.printException(CodeConst.codeToString(msg.what), e);
		}
		return true;
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