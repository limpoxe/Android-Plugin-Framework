package com.limpoxe.fairy.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.manager.PluginManagerProvider;

import java.util.List;

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
        return isPluginProcess(FairyGlobal.getHostApplication());
    }

    private static String getCurProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : list) {
            if (appProcess != null && appProcess.pid == android.os.Process.myPid()) {
                return appProcess.processName;
            }
        }
        return "";
    }

    private static String getPluginProcessName(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                //这里取个巧, 直接查询ContentProvider的信息中包含的processName
                //因为Contentprovider是被配置在插件进程的.
                //但是这个api只支持9及以上,
                ProviderInfo pinfo = context.getPackageManager().getProviderInfo(new ComponentName(context, PluginManagerProvider.class), 0);
                return pinfo==null?"":pinfo.processName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.printException("ProcessUtil.getPluginProcessName", e);
        }
        return "";
    }
}
