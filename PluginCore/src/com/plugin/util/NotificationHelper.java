package com.plugin.util;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.widget.RemoteViews;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginIntentResolver;
import com.plugin.core.PluginLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by cailiming on 16/1/10.
 */
public class NotificationHelper {
    /**
     * used before send notification
     * @param intent
     * @return
     */
    public static Intent resolveNotificationIntent(Intent intent, int type) {

        if (type == PluginDescriptor.BROADCAST) {

            Intent newIntent = PluginIntentResolver.resolveReceiver(intent).get(0);
            return newIntent;

        } else if (type == PluginDescriptor.ACTIVITY) {

            PluginIntentResolver.resolveActivity(intent);
            return intent;

        } else if (type == PluginDescriptor.SERVICE) {

            PluginIntentResolver.resolveService(intent);
            return intent;

        }
        return intent;
    }

    @SuppressWarnings("ResourceType")
    public static PendingIntent resolvePendingIntent(PendingIntent origin, int type) {
        if (origin != null) {
            Intent originIntent = (Intent)RefInvoker.invokeMethod(origin,
                    PendingIntent.class.getName(), "getIntent",
                    (Class[])null, (Object[])null);
            if (originIntent != null) {
                //如果目标是插件中的组件，需要额外提供2个参数, 默认为0、Update_Current。
                ArrayList<String> classNameList = PluginLoader.matchPlugin(originIntent, type);
                if (classNameList != null && classNameList.size() > 0) {

                    int requestCode = originIntent.getIntExtra("pending_requestCode", 0);
                    int flags = originIntent.getIntExtra("pending_flag", PendingIntent.FLAG_UPDATE_CURRENT);

                    if (type == PluginDescriptor.BROADCAST) {

                        Intent newIntent = PluginIntentResolver.resolveReceiver(originIntent).get(0);
                        return PendingIntent.getBroadcast(PluginLoader.getApplicatoin(), requestCode, newIntent, flags);

                    } else if (type == PluginDescriptor.ACTIVITY) {

                        PluginIntentResolver.resolveActivity(originIntent);
                        return PendingIntent.getActivity(PluginLoader.getApplicatoin(), requestCode, originIntent, flags);

                    } else if (type == PluginDescriptor.SERVICE) {

                        PluginIntentResolver.resolveService(originIntent);
                        return PendingIntent.getService(PluginLoader.getApplicatoin(), requestCode, originIntent, flags);

                    }
                }
            }
        }
        return origin;
    }


    public static RemoteViews createRemoteViews(int pluginNotificationLayout, String notificationResPath, String pluginId) {

        return createRemoteViews(pluginNotificationLayout, getNotificationResourcePath(pluginId, notificationResPath));
    }

    public static RemoteViews createRemoteViews(int pluginNotificationLayout, String notificationResPath) {

        RemoteViews remoteViews = new RemoteViews(PluginLoader.getApplicatoin().getPackageName(),
                pluginNotificationLayout);

        if (Build.VERSION.SDK_INT >=21) {

            ApplicationInfo applicationInfo = (ApplicationInfo) RefInvoker.getFieldObject(remoteViews, RemoteViews.class.getName(), "mApplication");
            if (applicationInfo != null) {
                ApplicationInfo newInfo = new ApplicationInfo();//重新构造一个，而不是修改原本的
                newInfo.packageName = applicationInfo.packageName;
                newInfo.sourceDir = applicationInfo.sourceDir;
                newInfo.dataDir = applicationInfo.dataDir;
                newInfo.publicSourceDir = notificationResPath;//要确保worldReadablePath这个路径可以被SystemUI应用读取，
                RefInvoker.setFieldObject(remoteViews, RemoteViews.class.getName(), "mApplication", newInfo);
            }
        }
        return remoteViews;
    }

    private static String getNotificationResourcePath(String pluginId, String worldReadablePath) {

        String pluginSrc = PluginLoader.getPluginDescriptorByPluginId(pluginId).getInstalledPath();

        File worldReadableFile = new File(worldReadablePath);

        if (FileUtil.copyFile(pluginSrc, worldReadableFile.getAbsolutePath())) {
            return worldReadableFile.getAbsolutePath();
        } else {
            //should not happen
            return pluginSrc;
        }
    }
}
