package com.limpoxe.fairy.core.compat;

import android.view.View;

import androidx.collection.SimpleArrayMap;

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
            AppCompatViewInflater = Class.forName(androidx_app_AppCompatViewInflater);
            Object cache = RefInvoker.getField(null, AppCompatViewInflater,
                androidx_app_AppCompatViewInflater_sConstructorMap);
            if (cache != null) {
                if (cache instanceof Map) {
                    EmptyHashMap<String, Constructor<? extends View>> newCacheMap = new EmptyHashMap<String, Constructor<? extends View>>();
                    newCacheMap.putAll((Map)cache);
                    RefInvoker.setField(null, AppCompatViewInflater,
                        androidx_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
                } else if (cache instanceof SimpleArrayMap) {
                    EmptySimpleArrayMap<String, Constructor<? extends View>> newCacheMap = new EmptySimpleArrayMap<String, Constructor<? extends View>>();
                    newCacheMap.putAll((SimpleArrayMap)cache);
                    RefInvoker.setField(null, AppCompatViewInflater,
                        androidx_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
                }
                return;
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForSupportv7ViewInflater.installAndroidXPluginCustomViewConstructorCache", e);
        }

        try {
            AppCompatViewInflater = Class.forName(android_support_v7_app_AppCompatViewInflater);
            Object cache = RefInvoker.getField(null, AppCompatViewInflater,
                    android_support_v7_app_AppCompatViewInflater_sConstructorMap);
            if (cache != null) {
                if (cache instanceof Map) {
                    EmptyHashMap<String, Constructor<? extends View>> newCacheMap = new EmptyHashMap<String, Constructor<? extends View>>();
                    newCacheMap.putAll((Map)cache);
                    RefInvoker.setField(null, AppCompatViewInflater,
                        android_support_v7_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
                } else if (cache instanceof SimpleArrayMap) {
                    EmptySimpleArrayMap<String, Constructor<? extends View>> newCacheMap = new EmptySimpleArrayMap<String, Constructor<? extends View>>();
                    newCacheMap.putAll((SimpleArrayMap)cache);
                    RefInvoker.setField(null, AppCompatViewInflater,
                        android_support_v7_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
                }
                return;
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForSupportv7ViewInflater.installPluginCustomViewConstructorCache", e);
        }
    }

    public static void clearViewInflaterConstructorCache() {
        try {
            Class AppCompatViewInflater = Class.forName(androidx_app_AppCompatViewInflater);
            Object sConstructorMap = RefInvoker.getField(null, AppCompatViewInflater,
                androidx_app_AppCompatViewInflater_sConstructorMap);
            if (sConstructorMap == null) {
                AppCompatViewInflater = Class.forName(android_support_v7_app_AppCompatViewInflater);
                sConstructorMap = RefInvoker.getField(null, AppCompatViewInflater,
                    android_support_v7_app_AppCompatViewInflater_sConstructorMap);
            }
            if (sConstructorMap != null) {
                RefInvoker.invokeMethod(sConstructorMap, sConstructorMap.getClass(), "clear", (Class[])null, (Object[])null);
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForSupportv7ViewInflater.clearViewInflaterConstructorCache", e);
        }
    }

}
