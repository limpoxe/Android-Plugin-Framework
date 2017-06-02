package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.example.plugintest.IMyAidlInterface;
import com.example.plugintest.Log;
import com.example.plugintest.R;
import com.example.plugintest.vo.ParamVO;
import com.limpoxe.fairy.util.LogUtil;

/**
 * @author cailiming
 * 
 */
public class PluginTestService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(
				this,
				" PluginTestService onCreate " + getResources().getText(R.string.hello_world3), Toast.LENGTH_LONG).show();

		Log.d("xx", "PluginTestService onCreate" + getApplication() + " "+ getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Log.d("xx", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());
		}

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
		Log.d("xx", "PluginTestService onDestroy");
		Toast.makeText(
				this,
				"停止PluginTestService", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new IMyAidlInterface.Stub() {

			@Override
			public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
				LogUtil.d(aString, anInt, aLong);
			}
		};
	}

}
