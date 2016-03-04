package com.plugin.util;

import android.app.ActivityManager;
import android.content.Context;

import com.plugin.core.PluginLoader;

public class ProcessUtil {

    private static final boolean sIsMultiProcessEnabled = true;

    private static Boolean isPluginProcess;

    public static boolean isPluginProcess() {

        if (!sIsMultiProcessEnabled) {
            return true;
        }

        if (isPluginProcess == null) {
            String processName = getCurProcessName(PluginLoader.getApplicatoin());
            isPluginProcess = processName.endsWith(":plugin");
        }
        return isPluginProcess;
    }

    private static String getCurProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == android.os.Process.myPid()) {
                return appProcess.processName;
            }
        }
        return "";
    }
}
