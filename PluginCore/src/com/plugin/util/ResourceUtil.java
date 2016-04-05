package com.plugin.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.PluginPublicXmlConst;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cailiming
 */
public class ResourceUtil {

    public static String getString(String value, Context pluginContext) {
        String idHex = null;
        if (value != null && value.startsWith("@") && value.length() == 9) {
            idHex = value.replace("@", "");

        } else if (value != null && value.startsWith("@android:") && value.length() == 17) {
            idHex = value.replace("@android:", "");
        }

        if (idHex != null) {
            try {
                int id = Integer.parseInt(idHex, 16);
                //此时context可能还没有初始化
                if (pluginContext != null) {
                    String des = pluginContext.getString(id);
                    return des;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static Boolean getBoolean(String value, Context pluginContext) {
        String idHex = null;
        if (value != null && value.startsWith("@") && value.length() == 9) {
            idHex = value.replace("@", "");

        } else if (value != null && value.startsWith("@android:") && value.length() == 17) {
            idHex = value.replace("@android:", "");
        }

        if (idHex != null) {
            try {
                int id = Integer.parseInt(idHex, 16);
                //此时context可能还没有初始化
                if (pluginContext != null) {
                    return pluginContext.getResources().getBoolean(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (value != null) {
            return Boolean.parseBoolean(value);
        }

        return null;
    }

    public static int getResourceId(String value) {
        String idHex = null;
        if (value != null && value.startsWith("@") && value.length() == 9) {
            idHex = value.replace("@", "");

        } else if (value != null && value.startsWith("@android:") && value.length() == 17) {
            idHex = value.replace("@android:", "");
        }
        if (idHex != null) {
            try {
                int id = Integer.parseInt(idHex, 16);
                return id;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String getLabel(PluginDescriptor pd) {
        PackageManager pm = PluginLoader.getApplication().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(pd.getInstalledPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = pd.getInstalledPath();
            appInfo.publicSourceDir = pd.getInstalledPath();
            String label = pm.getApplicationLabel(appInfo).toString();
            if (label != null && label.equals(pd.getPackageName())) {
                //可能设置的lable是来自宿主的资源
                if (pd.getDescription() != null) {
                    int id = ResourceUtil.getResourceId(pd.getDescription());
                    if (id != 0) {
                        //再宿主中查一次
                        try {
                            label = PluginLoader.getApplication().getResources().getString(id);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return label;
        }
        return pd.getDescription();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Drawable getLogo(PluginDescriptor pd) {
        PackageManager pm = PluginLoader.getApplication().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(pd.getInstalledPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = pd.getInstalledPath();
            appInfo.publicSourceDir = pd.getInstalledPath();
            Drawable logo = pm.getApplicationLogo(appInfo);
            return logo;
        }
        return null;
    }

    public static boolean isMainResId(int resid) {
        //如果使用的使openatlasextention
        //默认宿主的资源id以0x7f3X开头
        return PluginPublicXmlConst.resourceMap.get(resid>>16) != null;
    }

    public static void rewriteRValues(ClassLoader cl, String packageName, int id) {
        final Class<?> rClazz;
        try {
            rClazz = cl.loadClass(packageName + ".R");
        } catch (ClassNotFoundException e) {
            LogUtil.d("No resource references to update in package " + packageName);
            return;
        }

        final Method callback;
        try {
            callback = rClazz.getMethod("onResourcesLoaded", int.class);
        } catch (NoSuchMethodException e) {
            // No rewriting to be done.
            return;
        }

        Throwable cause;
        try {
            callback.invoke(null, id);
            return;
        } catch (IllegalAccessException e) {
            cause = e;
        } catch (InvocationTargetException e) {
            cause = e.getCause();
        }

        throw new RuntimeException("Failed to rewrite resource references for " + packageName,
                cause);
    }
}
