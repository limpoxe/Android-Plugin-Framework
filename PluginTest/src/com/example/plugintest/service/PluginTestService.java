package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * @author cailiming
 * 
 */
public class PluginTestService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("PluginTestService", "PluginTestService onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d("PluginTestService", "PluginTestService onStartCommand " 
					+ (intent == null?" null" : intent.toUri(0)));
		
		Toast.makeText(this, " PluginTestService " 
					+ (intent == null?" null" : intent.toUri(0)), Toast.LENGTH_LONG).show();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
