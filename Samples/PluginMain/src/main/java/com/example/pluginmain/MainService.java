package com.example.pluginmain;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("MainService", "打开插件PluginTestService");
		Intent serviceIntent = new Intent("test.lmn");
		serviceIntent.setPackage("com.example.plugintest");
		startService(serviceIntent);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
