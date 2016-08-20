package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.plugintest.R;

/**
 * @author cailiming
 * 
 */
public class PluginTestService2 extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(
				this,
				" PluginTestService2 "
						+ getResources().getText(R.string.hello_world3), Toast.LENGTH_LONG).show();
		Log.d("xx", "PluginTestService2 onCreate2" + getApplication() + " "+ getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {
			Log.d("xx", "PluginTestService2 onStartCommand2 "
					+ (intent == null ? " null" : intent.toUri(0)) + " " + getResources().getText(R.string.hello_world3));

			Toast.makeText(
					this,
					" PluginTestService2 "
							+ (intent == null ? " null" : (getResources().getText(R.string.hello_world3) + "," + intent
							.toUri(0))), Toast.LENGTH_LONG).show();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("xx", "PluginTestService2 onDestroy");
	}
}
