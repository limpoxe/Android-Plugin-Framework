package com.plugin.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;

import com.plugin.core.PluginLoader;
import com.plugin.core.manager.PluginManagerProvider;

public class ProcessUtil {

    private static Boolean isPluginProcess;

    public static boolean isPluginProcess(Context context) {

        if (isPluginProcess == null) {
            String processName = getCurProcessName(context);
            String pluginProcessName = getPluginProcessName(context);

            isPluginProcess = processName.equals(pluginProcessName);
        }
        return isPluginProcess;
    }

    public static boolean isPluginProcess() {
        return isPluginProcess(PluginLoader.getApplication());
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

    private static String getPluginProcessName(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                ProviderInfo pinfo = context.getPackageManager().getProviderInfo(new ComponentName(context, PluginManagerProvider.class), 0);
                return pinfo.processName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
