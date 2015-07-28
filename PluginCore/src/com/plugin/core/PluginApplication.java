package com.plugin.core;

import com.plugin.util.RefInvoker;

import android.app.Application;
import android.os.Handler;

public class PluginApplication extends Application {
	
	private String mProcessName;
	
	@Override
	public void onCreate() {
		super.onCreate();	
		inject();
	}
	
	private void inject() {
		//从ThreadLocal中取出来的
		Object activityThread = RefInvoker.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread",
				(Class[])null, (Object[])null);
	
		mProcessName = (String)RefInvoker.invokeMethod(activityThread, "android.app.ActivityThread",
				"getProcessName", (Class[])null, (Object[])null);
	
		RefInvoker.setFieldObject(activityThread, "android.app.ActivityThread", "mInstrumentation",
				new PluginInstrumention());
		
		//getHandler
		Handler handler = (Handler)RefInvoker.getStaticFieldObject("android.app.ActivityThread", "sMainThreadHandler");
	
		//给handler添加一个callback
		RefInvoker.setFieldObject(handler, Handler.class.getName(), "mCallback", new PluginAppTrace(handler));
	
	}
	
	public String getProcessName() {
		return mProcessName;
	}

}
