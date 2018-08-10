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
                int id = (int)Long.parseLong(idHex, 16);
                //此时context可能还没有初始化
                if (pluginContext != null) {
                    String des = pluginContext.getString(id);
                    return des;
                }
            } catch (Exception e) {
                LogUtil.printException("ResourceUtil.getString", e);
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
                int id = (int)Long.parseLong(idHex, 16);
                //此时context可能还没有初始化
                if (pluginContext != null) {
                    return pluginContext.getResources().getBoolean(id);
                }
            } catch (Exception e) {
                LogUtil.printException("ResourceUtil.getBoolean", e);
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
        if (value != null && value.contains(":")) {
            idHex = value.split(":")[1];
        } else if (value != null && value.startsWith("@") && value.length() == 9) {
            idHex = value.replace("@", "");
        }
        if (idHex != null) {
            try {
                int id = (int)Long.parseLong(idHex, 16);
                return id;
            } catch (Exception e) {
                LogUtil.printException("ResourceUtil.parseResId", e);
            }
        }
        return 0;
    }

    public static String getLabel(PluginDescriptor pluginDescriptor) {
        PackageInfo info = pluginDescriptor.getPackageInfo(PackageManager.GET_ACTIVITIES);
        if (info != null) {
            String label = null;
            try {
                if (pluginDescriptor.isStandalone() || !isMainResId(info.applicationInfo.labelRes)){
                    label = FairyGlobal.getHostApplication().getPackageManager().getApplicationLabel(info.applicationInfo).toString();
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
                            label = FairyGlobal.getHostApplication().getResources().getString(id);
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

    public static Drawable getLogo(PluginDescriptor pluginDescriptor) {
        if (Build.VERSION.SDK_INT >= 9) {
            PackageInfo info = pluginDescriptor.getPackageInfo(PackageManager.GET_ACTIVITIES);
            if (info != null) {
                Drawable logo = FairyGlobal.getHostApplication().getPackageManager().getApplicationLogo(info.applicationInfo);
                return logo;
            }
        }
        return null;
    }

    public static Drawable getIcon(PluginDescriptor pluginDescriptor) {
        if (Build.VERSION.SDK_INT >= 9) {
            PackageManager pm = FairyGlobal.getHostApplication().getPackageManager();
            PackageInfo info = pluginDescriptor.getPackageInfo(PackageManager.GET_ACTIVITIES);
            if (info != null) {
                Drawable logo = pm.getApplicationIcon(info.applicationInfo);
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

    public static String covent2Hex(String resId) {
        if (resId == null) {
            return null;
        }
        if (resId.startsWith("@")) {
            if (resId.contains(":")) {
                String[] idStr = resId.split(":");
                //size一定等于2，不等于2的情况我也管不着啦
                idStr[1] = Long.toHexString(Long.parseLong(idStr[1]) & 0xFFFFFFFFL);
                return idStr[0] + ":" + lengthAlign(idStr[1]);
            } else {
                String[] idStr = resId.split("@");
                //size一定等于2，不等于2的情况我也管不着啦
                idStr[1] = Long.toHexString(Long.parseLong(idStr[1]) & 0xFFFFFFFFL);
                return "@" + lengthAlign(idStr[1]);
            }
        } else {
            return resId;
        }
    }

    private static String lengthAlign(String idStr) {
        if (idStr.length() < 8) {//PPTTNNNN
            int pad = 8 - idStr.length();
            String fill = "";
            for(int i = 0; i < pad; i++) {
                fill = fill + "0";
            }
            idStr = fill + idStr;
        }
        return idStr;
    }
}
