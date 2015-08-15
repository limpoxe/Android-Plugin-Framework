package com.plugin.core;

import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;

import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

public class PluginApplication extends Application {

	private String mProcessName;

	public String getProcessName() {
		return mProcessName;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		injectInstrumentation();
	}

	/**
	 * 注入Instrumentation主要是为了支持Activity
	 */
	private void injectInstrumentation() {
		LogUtil.d("injectInstrumentation");

		// 从ThreadLocal中取出来的
		Object activityThread = RefInvoker.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread",
				(Class[]) null, (Object[]) null);

		mProcessName = (String) RefInvoker.invokeMethod(activityThread, "android.app.ActivityThread", "getProcessName",
				(Class[]) null, (Object[]) null);

		// 给Instrumentation添加一层代理，用来实现隐藏api的调用
		Instrumentation originalInstrumentation = (Instrumentation) RefInvoker.getFieldObject(activityThread,
				"android.app.ActivityThread", "mInstrumentation");
		RefInvoker.setFieldObject(activityThread, "android.app.ActivityThread", "mInstrumentation",
				new PluginInstrumentionWrapper(originalInstrumentation));

		// getHandler
		Handler handler = (Handler) RefInvoker.getStaticFieldObject("android.app.ActivityThread", "sMainThreadHandler");

		// 给handler添加一个callback
		RefInvoker.setFieldObject(handler, Handler.class.getName(), "mCallback", new PluginAppTrace(handler));

	}

	@Override
	public void sendBroadcast(Intent intent) {
		LogUtil.d("sendBroadcast", intent.toUri(0));
		intent = PluginIntentResolver.resolveReceiver(intent);
		super.sendBroadcast(intent);
	}

	@Override
	public ComponentName startService(Intent service) {
		LogUtil.d("startService", service.toUri(0));
		PluginIntentResolver.resolveService(service);
		return super.startService(service);
	}

	@Override
	public boolean stopService(Intent name) {
		LogUtil.d("stopService", name.toUri(0));
		if (PluginIntentResolver.resolveStopService(name)) {
			super.startService(name);
			return false;
		} else {
			return super.stopService(name);
		}
	}

	/**
	 * startActivity有很多重载的方法，如有必要，可以相应的进行重写
	 */
	@Override
	public void startActivity(Intent intent) {
		LogUtil.d("startActivity", intent.toUri(0));
		PluginIntentResolver.resloveActivity(intent);
		super.startActivity(intent);
	}

}
