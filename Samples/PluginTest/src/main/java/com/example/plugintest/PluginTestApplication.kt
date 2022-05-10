package com.example.plugintest

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.multidex.MultiDex
import com.limpoxe.fairy.util.FakeUtil
import com.limpoxe.fairy.util.LogUtil

class PluginTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PluginTestApplication onCreate, $applicationContext")
        printProcessName()
    }

    private fun printProcessName() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val list = activityManager.runningAppProcesses
        for (appProcess in list) {
            if (appProcess.pid == Process.myPid()) {
                Log.d("PluginTestApplication processName=" + appProcess.processName)
                break
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(FakeUtil.fakeMultiDexContext(this))
    }

    override fun onTerminate() {
        super.onTerminate()
        LogUtil.e("PluginTestApplication onTerminate")
    }
}