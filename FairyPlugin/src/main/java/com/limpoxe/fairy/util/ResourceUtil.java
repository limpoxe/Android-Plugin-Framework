package com.limpoxe.fairy.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;

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

    /**
     * use parseResId() instead
     */
    @Deprecated
    public static int getResourceId(String value) {
        return parseResId(value);
    }

    public static int parseResId(String value) {
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

    public static String getLabel(PluginDescriptor pluginDescriptor) {
        PackageManager pm = FairyGlobal.getApplication().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(pluginDescriptor.getInstalledPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = pluginDescriptor.getInstalledPath();
            appInfo.publicSourceDir = pluginDescriptor.getInstalledPath();
            String label = null;
            try {
                if (pluginDescriptor.isStandalone() || !isMainResId(appInfo.labelRes)){
                    label = pm.getApplicationLabel(appInfo).toString();
                }
            } catch (Resources.NotFoundException e) {
            }
            if (label == null || label.equals(pluginDescriptor.getPackageName())) {
                //可能设置的lable是来自宿主的资源
                if (pluginDescriptor.getDescription() != null) {
                    int id = ResourceUtil.parseResId(pluginDescriptor.getDescription());
                    if (id != 0) {
                        //再宿主中查一次
                        try {
                            label = FairyGlobal.getApplication().getResources().getString(id);
                        } catch (Resources.NotFoundException e) {
                        }
                    }
                }
            }
            if (label != null) {
                return label;
            }
        }
        return pluginDescriptor.getDescription();
    }

    public static Bundle getApplicationMetaData(String apkPath) {
        //暂时只查询Applicatoin节点下的meta信息，其他组件节点下的meta先不管
        PackageInfo info = FairyGlobal.getApplication().getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_META_DATA);
        if (info != null && info.applicationInfo != null) {
            return info.applicationInfo.metaData;
        }
        return null;
    }

    public static Drawable getLogo(PluginDescriptor pd) {
        if (Build.VERSION.SDK_INT >= 9) {
            PackageManager pm = FairyGlobal.getApplication().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(pd.getInstalledPath(), PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = pd.getInstalledPath();
                appInfo.publicSourceDir = pd.getInstalledPath();
                Drawable logo = pm.getApplicationLogo(appInfo);
                return logo;
            }
        }
        return null;
    }

    public static Drawable getIcon(PluginDescriptor pd) {
        if (Build.VERSION.SDK_INT >= 9) {
            PackageManager pm = FairyGlobal.getApplication().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(pd.getInstalledPath(), PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = pd.getInstalledPath();
                appInfo.publicSourceDir = pd.getInstalledPath();
                Drawable logo = pm.getApplicationIcon(appInfo);
                return logo;
            }
        }
        return null;
    }

    public static boolean isMainResId(int resid) {
        int packageId = resid >> 24;
        if (packageId != 0x7f) {//加这个判断是为了支持通过修改aapt的方式进行资源分组
            return false;
        }

        //这里之所以这样判断是因为 宿主的public.xml中限制了宿主的资源id范围
        //如果public.xml配置在插件中, 这里需要将这个判断反过来
        return resid>>16 > 0x7f2F || resid>>16 == 0x7f01;
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
