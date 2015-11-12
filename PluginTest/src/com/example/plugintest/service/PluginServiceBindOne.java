package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * BindService
 */
public class PluginServiceBindOne extends Service {

    private static final String TAG = PluginServiceBindOne.class.getSimpleName();

    private MyBinderOne myBinderOne = new MyBinderOne();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return myBinderOne;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CommServiceBind onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    public class MyBinderOne extends Binder {

        public PluginServiceBindOne getService() {
            return PluginServiceBindOne.this;
        }
    }

    public void showToast() {
        Toast.makeText(getApplicationContext(), TAG + " Toast",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


}
