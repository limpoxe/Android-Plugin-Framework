package com.example.plugintest;

import android.app.Application;
import android.util.Log;

import com.plugin.core.localservice.LocalServiceManager;

public class PluginTestApplication extends  Application {
	
	@Override
	public void onCreate() {
		super.onCreate();

		/**
		 * 其他插件可通过LocalServiceManager.getService("plugin_test_service")获得这个service
		 */
		LocalServiceManager.registerService("plugin_login_service", new PluginLoginService());

		Log.d("PluginTestApplication", "PluginTestApplication onCreate called");
	}

}
