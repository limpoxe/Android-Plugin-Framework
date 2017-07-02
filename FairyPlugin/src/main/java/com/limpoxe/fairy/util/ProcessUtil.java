package com.limpoxe.fairy.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.manager.PluginManagerProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;

public class ProcessUtil {

    private static Boolean isPluginProcess;

    public static boolean isPluginProcess(Context context) {

        if (isPluginProcess == null) {
            String processName = getCurProcessName();
            String pluginProcessName = getPluginProcessName(context);

            isPluginProcess = processName.equals(pluginProcessName);
        }
        return isPluginProcess;
    }

    public static boolean isPluginProcess() {
        return isPluginProcess(FairyGlobal.getApplication());
    }

    private static String getCurProcessName() {
        BufferedReader mBufferedReader=null;
	       final int pid = android.os.Process.myPid();
	       try {
                       File file = new File("proc/" + pid + "/" + "cmdline");
                       mBufferedReader = new BufferedReader(new FileReader(file));
                       String processName = mBufferedReader.readLine().trim();
                       mBufferedReader.close();
                       return processName;
               } catch (Exception e) {
                       e.printStackTrace();

               } finally {
                       if (mBufferedReader != null) {
                               try {
                                       mBufferedReader.close();
                               } catch (IOException e) {
                                       e.printStackTrace();
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
                return pinfo.processName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
