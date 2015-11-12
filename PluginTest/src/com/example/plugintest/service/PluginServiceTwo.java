package com.example.plugintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PluginServiceTwo extends Service {

    private static final String TAG = PluginServiceTwo.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,
                "PluginServiceTwo onStartCommand:"+intent.getStringExtra("Params"), Toast.LENGTH_SHORT).show();

        final Intent i = intent;
        new Thread() {

            @Override
            public void run() {
                int it = 0;
                while (it < 5) {
                    it++;
                    Log.d(TAG,
                            "PluginServiceTwo onStartCommand:,任务" + it);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.start();

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

}
