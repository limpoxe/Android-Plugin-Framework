package com.limpoxe.fairy.core.proxy.systemservice;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginIntentResolver;
import com.limpoxe.fairy.core.android.HackNotificationManager;
import com.limpoxe.fairy.core.android.HackPendingIntent;
import com.limpoxe.fairy.core.android.HackRemoteViews;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.FileUtil;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;
import com.limpoxe.fairy.util.ResourceUtil;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppINotificationManager extends MethodProxy {

    static {
        sMethods.put("enqueueNotification", new enqueueNotification());
        sMethods.put("enqueueNotificationWithTag", new enqueueNotificationWithTag());
        sMethods.put("enqueueNotificationWithTagPriority", new enqueueNotificationWithTagPriority());
    }

    public static void installProxy() {
        LogUtil.d("安装NotificationManagerProxy");
        Object androidAppINotificationStubProxy = HackNotificationManager.getService();
        Object androidAppINotificationStubProxyProxy = ProxyUtil.createProxy(androidAppINotificationStubProxy, new AndroidAppINotificationManager());
        HackNotificationManager.setService(androidAppINotificationStubProxyProxy);
        LogUtil.d("安装完成");
    }

    public static class enqueueNotification extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            args[0] = FairyGlobal.getApplication().getPackageName();
            for(Object obj: args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification)obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTag extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            args[0] = FairyGlobal.getApplication().getPackageName();
            for(Object obj: args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification)obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTagPriority extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            args[0] = FairyGlobal.getApplication().getPackageName();
            for(Object obj: args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification)obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    private static void resolveRemoteViews(Notification notification) {

        String hostPackageName = FairyGlobal.getApplication().getPackageName();

        if (Build.VERSION.SDK_INT >= 23) {
            Icon mSmallIcon = (Icon)RefInvoker.getField(notification, Notification.class, "mSmallIcon");
            Icon mLargeIcon = (Icon)RefInvoker.getField(notification, Notification.class, "mLargeIcon");
            if (mSmallIcon != null) {
                RefInvoker.setField(mSmallIcon, Icon.class, "mString1", hostPackageName);
            }
            if (mLargeIcon != null) {
                RefInvoker.setField(mLargeIcon, Icon.class, "mString1", hostPackageName);
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {

            int layoutId = 0;
            if (notification.tickerView != null) {
                layoutId = new HackRemoteViews(notification.tickerView).getLayoutId();
            }
            if (layoutId == 0) {
                if (notification.contentView != null) {
                    layoutId = new HackRemoteViews(notification.contentView).getLayoutId();
                }
            }
            if (layoutId == 0) {
                if (notification.bigContentView != null) {
                    layoutId = new HackRemoteViews(notification.bigContentView).getLayoutId();
                }
            }
            if (layoutId == 0) {
                if (notification.headsUpContentView != null) {
                    layoutId = new HackRemoteViews(notification.headsUpContentView).getLayoutId();
                }
            }

            if (layoutId == 0) {
                return;
            }

            //检查资源布局资源Id是否属于宿主
            if (ResourceUtil.isMainResId(layoutId)) {
                return;
            }

            //检查资源布局资源Id是否属于系统
            if (layoutId >> 24 == 0x1f) {
                return;
            }

            if ("Xiaomi".equals(Build.MANUFACTURER)) {
                LogUtil.e("Xiaomi, not support, caused by MiuiResource");
                if (notification.contentView != null) {
                    //重置layout，避免crash
                    new HackRemoteViews(notification.contentView).setLayoutId(android.R.layout.test_list_item);
                }
                notification.bigContentView = null;
                notification.headsUpContentView = null;
                notification.tickerView = null;
                return;
            }

            ApplicationInfo newInfo = new ApplicationInfo();
            String packageName = null;

            if (notification.tickerView != null) {
                packageName = notification.tickerView.getPackage();
                new HackRemoteViews(notification.tickerView).setApplicationInfo(newInfo);
            }
            if (notification.contentView != null) {
                if (packageName == null) {
                    packageName = notification.contentView.getPackage();
                }
                new HackRemoteViews(notification.contentView).setApplicationInfo(newInfo);
            }
            if (notification.bigContentView != null) {
                if (packageName == null) {
                    packageName = notification.bigContentView.getPackage();
                }
                new HackRemoteViews(notification.bigContentView).setApplicationInfo(newInfo);
            }
            if (notification.headsUpContentView != null) {
                if (packageName == null) {
                    packageName = notification.headsUpContentView.getPackage();
                }
                new HackRemoteViews(notification.headsUpContentView).setApplicationInfo(newInfo);
            }

            ApplicationInfo applicationInfo = FairyGlobal.getApplication().getApplicationInfo();
            newInfo.packageName = applicationInfo.packageName;
            newInfo.sourceDir = applicationInfo.sourceDir;
            newInfo.dataDir = applicationInfo.dataDir;

            if (packageName != null && !packageName.equals(hostPackageName)) {

                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                newInfo.packageName = pluginDescriptor.getPackageName();
                //要确保publicSourceDir这个路径可以被SystemUI应用读取，
                newInfo.publicSourceDir = prepareNotificationResourcePath(pluginDescriptor.getInstalledPath(),
                        FairyGlobal.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");

            } else if (packageName != null && packageName.equals(hostPackageName)) {
                //如果packageName是宿主，由于前面已经判断出，layoutid不是来自插件，则尝试查找notifications的目标页面，如果目标是插件，则尝试使用此插件作为通知栏的资源来源
                if (notification.contentIntent != null) {//只处理contentIntent，其他不管
                    Intent intent = new HackPendingIntent(notification.contentIntent).getIntent();
                    if (intent != null && intent.getAction() != null && intent.getAction().contains(PluginIntentResolver.CLASS_SEPARATOR)) {
                        String className = intent.getAction().split(PluginIntentResolver.CLASS_SEPARATOR)[0];
                        PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                        newInfo.packageName = pluginDescriptor.getPackageName();
                        //要确保publicSourceDir这个路径可以被SystemUI应用读取，
                        newInfo.publicSourceDir = prepareNotificationResourcePath(pluginDescriptor.getInstalledPath(),
                                FairyGlobal.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= 11)  {
            if (notification.tickerView != null) {
                new HackRemoteViews(notification.tickerView).setPackage(hostPackageName);
            }
            if (notification.contentView != null) {
                new HackRemoteViews(notification.contentView).setPackage(hostPackageName);
            }
        }
    }

    private static String prepareNotificationResourcePath(String pluginInstalledPath, String worldReadablePath) {
        LogUtil.w("正在为通知栏准备插件资源。。。这里现在暂时是同步复制，注意大文件卡顿！！");
        File worldReadableFile = new File(worldReadablePath);

        if (FileUtil.copyFile(pluginInstalledPath, worldReadableFile.getAbsolutePath())) {
            LogUtil.w("通知栏插件资源准备完成，请确保此路径SystemUi有读权限", worldReadableFile.getAbsolutePath());
            return worldReadableFile.getAbsolutePath();
        } else {
            LogUtil.e("不应该到这里来，直接返回这个路径SystemUi没有权限读取");
            return pluginInstalledPath;
        }
    }

}
