package com.example.plugintest;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import com.limpoxe.fairy.util.LogUtil;

import java.util.List;

public class  PluginTestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PluginTestApplication onCreate, $applicationContext");
        printProcessName();
    }

    private void printProcessName() {
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : list) {
            if (appProcess.pid == Process.myPid()) {
                Log.d("PluginTestApplication processName=" + appProcess.processName);
                break;
            }
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.e("PluginTestApplication onTerminate");
    }
}