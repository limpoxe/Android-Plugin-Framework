package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


/**
 * BindProcessService
 */
public class PluginServiceProcessBindOne extends Service {

    private static final String TAG = PluginServiceProcessBindOne.class.getSimpleName();

    private final IServiceProcessBindOne.Stub mBinder = new IServiceProcessBindOne.Stub() {

        @Override
        public void showToast() throws RemoteException {
            handler.sendEmptyMessage(0);
        }

        @Override
        public int calculate(int a, int b) throws RemoteException {
            return a + b;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int pid = android.os.Process.myPid();
        Log.d(TAG, "onCreate pid:" + pid);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CommServiceBind onStartCommand");
        return super.onStartCommand(intent, flags, startId);
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

    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int pid = android.os.Process.myPid();
            Toast.makeText(getApplicationContext(), TAG + " pid:" + pid, Toast.LENGTH_SHORT).show();
        }
    };

}
