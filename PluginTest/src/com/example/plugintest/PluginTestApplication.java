package com.example.plugintest;

import android.app.Application;
import android.content.Context;

import com.plugin.util.LogUtil;

public class PluginTestApplication extends  Application {
	
	@Override
	public void onCreate() {
		super.onCreate();

		Context ctx = getApplicationContext();
		LogUtil.d("ctx", ctx);
	}



}
