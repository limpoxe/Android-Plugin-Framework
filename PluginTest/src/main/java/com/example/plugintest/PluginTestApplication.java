package com.example.plugintest;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.limpoxe.fairy.util.LogUtil;

public class PluginTestApplication extends  Application {

	private String packageName;

	@Override
	public void onCreate() {
		super.onCreate();

		Context ctx = getApplicationContext();
		Log.d("xx", "" + ctx);

		if (isApplicationProcess()) {
			LogUtil.d("api欺骗成功，让插件以为自己在主进程运行");
		}

	}

	private boolean isApplicationProcess() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
			if (appProcess.pid == android.os.Process.myPid()) {
				if (appProcess.processName.equals(packageName)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		packageName = getPackageName();
	}
}
