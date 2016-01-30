package com.plugin.util;

import android.content.Context;

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
