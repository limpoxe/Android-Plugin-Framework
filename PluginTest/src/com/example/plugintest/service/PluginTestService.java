package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.plugintest.R;
import com.example.plugintest.vo.ParamVO;

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

		Log.d("xx", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());

		Log.d("PluginTestService", "PluginTestService onStartCommand "
				+ " " + getResources().getText(R.string.hello_world3));

		Toast.makeText(
				this,
				" PluginTestService " + getResources().getText(R.string.hello_world3), Toast.LENGTH_LONG).show();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(
				this,
				"停止PluginTestService", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
