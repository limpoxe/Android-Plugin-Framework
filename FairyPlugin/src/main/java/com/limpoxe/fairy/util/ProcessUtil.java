package com.limpoxe.fairy.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.text.TextUtils;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.manager.PluginManagerProvider;

import java.util.List;

public class ProcessUtil {

    private static Boolean isPluginProcess;

    /**
     * 因为判断进程的需要依赖activityManager.getRunningAppProcesses方法
     * 而此方法又会被框架hook，所以这个方法必需在框架hookgetRunningAppProcesses前先hook一次，并将结果缓存到成员变量中
     * 这样才能不受hook影响
     * @param context
     * @return
     */
    public static boolean isPluginProcess(Context context) {

        if (isPluginProcess == null) {
            String processName = getCurProcessName(context);
            String pluginProcessName = getPluginProcessName(context);

            if (TextUtils.isEmpty(processName) || TextUtils.isEmpty(pluginProcessName)) {
                LogUtil.e("a fatal error happened, should throw an exception here?", "processName:" + pluginProcessName + ", pluginProcessName:" + pluginProcessName);
            }

            isPluginProcess = processName.equals(pluginProcessName);
        }
        return isPluginProcess;
    }

    /**
     * 这个方法能正确判断当前插件是否为插件进程，是因为在hook插件的进程的判断方法前（getRunningProcess），已经判断并
     * 缓存了当前进程是否为插件进程的结果
     * @return
     */
    public static boolean isPluginProcess() {
        return isPluginProcess(FairyGlobal.getHostApplication());
    }

    private static String getCurProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : list) {
                if (appProcess != null && appProcess.pid == android.os.Process.myPid()) {
                    return appProcess.processName;
                }
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
