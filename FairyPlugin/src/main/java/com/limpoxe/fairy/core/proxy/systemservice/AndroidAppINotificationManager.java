package com.limpoxe.fairy.core.proxy.systemservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.widget.RemoteViews;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginIntentResolver;
import com.limpoxe.fairy.core.PluginLoader;
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
        Object androidAppINotificationStubProxy = RefInvoker.invokeStaticMethod(NotificationManager.class.getName(), "getService", (Class[])null, (Object[])null);
        Object androidAppINotificationStubProxyProxy = ProxyUtil.createProxy(androidAppINotificationStubProxy, new AndroidAppINotificationManager());
        RefInvoker.setStaticObject(NotificationManager.class.getName(), "sService", androidAppINotificationStubProxyProxy);
        LogUtil.d("安装完成");
    }

    public static class enqueueNotification extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
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
            args[0] = PluginLoader.getApplication().getPackageName();
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
            args[0] = PluginLoader.getApplication().getPackageName();
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

        if (Build.VERSION.SDK_INT >= 23) {
            Icon mSmallIcon = (Icon)RefInvoker.getFieldObject(notification, Notification.class, "mSmallIcon");
            Icon mLargeIcon = (Icon)RefInvoker.getFieldObject(notification, Notification.class, "mLargeIcon");
            if (mSmallIcon != null) {
                RefInvoker.setFieldObject(mSmallIcon, Icon.class, "mString1", PluginLoader.getApplication().getPackageName());
            }
            if (mLargeIcon != null) {
                RefInvoker.setFieldObject(mLargeIcon, Icon.class, "mString1", PluginLoader.getApplication().getPackageName());
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {

            int layoutId = 0;
            if (notification.tickerView != null) {
                layoutId = (int)RefInvoker.getFieldObject(notification.tickerView, RemoteViews.class, "mLayoutId");
            }
            if (layoutId == 0) {
                if (notification.contentView != null) {
                    layoutId = (int)RefInvoker.getFieldObject(notification.contentView, RemoteViews.class, "mLayoutId");
                }
            }
            if (layoutId == 0) {
                if (notification.bigContentView != null) {
                    layoutId = (int)RefInvoker.getFieldObject(notification.bigContentView, RemoteViews.class, "mLayoutId");
                }
            }
            if (layoutId == 0) {
                if (notification.headsUpContentView != null) {
                    layoutId = (int)RefInvoker.getFieldObject(notification.headsUpContentView, RemoteViews.class, "mLayoutId");
                }
            }

            if (layoutId != 0) {

                //检查资源布局资源Id是否属于宿主
                if (!ResourceUtil.isMainResId(layoutId)) {

                    if (Build.VERSION.SDK_INT > 23) {
                        LogUtil.e("not support");
                        notification.contentView = null;
                        notification.bigContentView = null;
                        notification.headsUpContentView = null;
                        notification.tickerView = null;
                        return;
                    }

                    ApplicationInfo newInfo = new ApplicationInfo();
                    String packageName = null;

                    if (notification.tickerView != null) {
                        packageName = notification.tickerView.getPackage();
                        RefInvoker.setFieldObject(notification.tickerView, RemoteViews.class.getName(), "mApplication", newInfo);
                    }
                    if (notification.contentView != null) {
                        if (packageName == null) {
                            packageName = notification.contentView.getPackage();
                        }
                        RefInvoker.setFieldObject(notification.contentView, RemoteViews.class.getName(), "mApplication", newInfo);
                    }
                    if (notification.bigContentView != null) {
                        if (packageName == null) {
                            packageName = notification.bigContentView.getPackage();
                        }
                        RefInvoker.setFieldObject(notification.bigContentView, RemoteViews.class.getName(), "mApplication", newInfo);
                    }
                    if (notification.headsUpContentView != null) {
                        if (packageName == null) {
                            packageName = notification.headsUpContentView.getPackage();
                        }
                        RefInvoker.setFieldObject(notification.headsUpContentView, RemoteViews.class.getName(), "mApplication", newInfo);
                    }

                    ApplicationInfo applicationInfo = PluginLoader.getApplication().getApplicationInfo();
                    newInfo.packageName = applicationInfo.packageName;
                    newInfo.sourceDir = applicationInfo.sourceDir;
                    newInfo.dataDir = applicationInfo.dataDir;

                    if (packageName != null && !packageName.equals(PluginLoader.getApplication().getPackageName())) {

                        PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                        //要确保publicSourceDir这个路径可以被SystemUI应用读取，
                        newInfo.publicSourceDir = prepareNotificationResourcePath(pd.getInstalledPath(),
                                PluginLoader.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");

                    } else if (packageName != null && packageName.equals(PluginLoader.getApplication().getPackageName())) {

                        Intent intent = (Intent)RefInvoker.invokeMethod(notification.contentIntent, PendingIntent.class.getName(), "getIntent", (Class[]) null, (Object[]) null);
                        if (intent != null && intent.getAction() != null && intent.getAction().contains(PluginIntentResolver.CLASS_SEPARATOR)) {
                            String className = intent.getAction().split(PluginIntentResolver.CLASS_SEPARATOR)[0];
                            PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByClassName(className);
                            //要确保publicSourceDir这个路径可以被SystemUI应用读取，
                            newInfo.publicSourceDir = prepareNotificationResourcePath(pd.getInstalledPath(),
                                    PluginLoader.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");
                        }
                    }
                }
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
