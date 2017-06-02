package com.example.pluginmain;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.pluginsharelib.IHostAidlInterface;
import com.limpoxe.fairy.util.LogUtil;

public class HostService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
        LogUtil.d("HostService onCreate");
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new IHostAidlInterface.Stub() {

            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
                Log.d("IHostAidlInterface", "basicTypes" + anInt + " " + aString);
            }
        };
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d("HostService onDestroy");
    }
}
