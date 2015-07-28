package com.example.plugintest;

import android.app.Application;
import android.util.Log;

public class PluginTestApplication extends  Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("PluginTestApplication", "PluginTestApplication onCreate called");
	}

}
