package com.limpoxe.fairy.core.compat;

import android.os.Debug;
import android.view.View;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * for supportv7
 */
public class CompatForSupportv7ViewInflater {

    private static final String android_support_v7_app_AppCompatViewInflater = "android.support.v7.app.AppCompatViewInflater";
    private static final String android_support_v7_app_AppCompatViewInflater_sConstructorMap = "sConstructorMap";

    private static final String androidx_app_AppCompatViewInflater = "androidx.appcompat.app.AppCompatViewInflater";
    private static final String androidx_app_AppCompatViewInflater_sConstructorMap = "sConstructorMap";

    public static void installPluginCustomViewConstructorCache() {
        Class AppCompatViewInflater = null;
        try {
            AppCompatViewInflater = Class.forName(android_support_v7_app_AppCompatViewInflater);
            Map cache = (Map) RefInvoker.getField(null, AppCompatViewInflater,
                    android_support_v7_app_AppCompatViewInflater_sConstructorMap);
            if (cache != null) {
                EmptyHashMap<String, Constructor<? extends View>> newCacheMap = new EmptyHashMap<String, Constructor<? extends View>>();
                newCacheMap.putAll(cache);
                RefInvoker.setField(null, AppCompatViewInflater,
                        android_support_v7_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
            }
        } catch (ClassNotFoundException e) {
            LogUtil.printException("CompatForSupportv7ViewInflater.installPluginCustomViewConstructorCache", e);
        }
    }

    public static void installAndroidXPluginCustomViewConstructorCache() {
        Class AppCompatViewInflater = null;
        try {
            AppCompatViewInflater = Class.forName(androidx_app_AppCompatViewInflater);
            Map cache = (Map) RefInvoker.getField(null, AppCompatViewInflater,
                    androidx_app_AppCompatViewInflater_sConstructorMap);
            if (cache != null) {
                EmptyHashMap<String, Constructor<? extends View>> newCacheMap = new EmptyHashMap<String, Constructor<? extends View>>();
                newCacheMap.putAll(cache);
                RefInvoker.setField(null, AppCompatViewInflater,
                        androidx_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
            }
        } catch (ClassNotFoundException e) {
            LogUtil.printException("CompatForSupportv7ViewInflater.installAndroidXPluginCustomViewConstructorCache", e);
        }
    }

}
