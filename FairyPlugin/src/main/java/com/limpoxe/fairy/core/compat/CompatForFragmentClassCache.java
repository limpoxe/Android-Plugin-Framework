package com.limpoxe.fairy.core.compat;

import android.content.Context;

import androidx.collection.SimpleArrayMap;

import com.limpoxe.fairy.util.RefInvoker;

import java.util.HashMap;
import java.util.Map;

/**
 * for supportv7
 */
public class CompatForFragmentClassCache {

    private static final String androidx_fragment_app_Fragment = "androidx.fragment.app.Fragment";
    private static final String androidx_fragment_app_Fragment_sClassMap = "sClassMap";

    private static final String androidx_fragment_app_FragmentFactory = "androidx.fragment.app.FragmentFactory";
    private static final String androidx_fragment_app_FragmentFactory_sClassMap = "sClassCacheMap";

    private static final String android_support_v4_app_Fragment = "android.support.v4.app.Fragment";
    private static final String android_support_v4_app_Fragment_sClassMap = "sClassMap";

    private static final String android_app_Fragment = "android.app.Fragment";
    private static final String android_app_Fragment_sClassMap = "sClassMap";

    //阻止class缓存
    public static void installFragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(android_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, android_app_Fragment_sClassMap);
            if (slCassMap != null) {
                //4.3及以下是 HashMap<String, Class<?>>
                if (slCassMap.getClass().isAssignableFrom(HashMap.class)) {
                    RefInvoker.setField(null, FragmentClass, android_app_Fragment_sClassMap, new EmptyHashMap<String, Class<?>>());
                } else {
                    //4.4+ android.util.ArrayMap<String, Class<?>>
                }
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.installFragmentClassCache", e);
        }
    }

    //阻止class缓存
    public static void installSupportV4FragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(android_support_v4_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, android_support_v4_app_Fragment_sClassMap);
            if (slCassMap != null) {
                //4.3及以下是 HashMap<String, Class<?>>
                if (slCassMap.getClass().isAssignableFrom(HashMap.class)) {
                    RefInvoker.setField(null, FragmentClass, android_support_v4_app_Fragment_sClassMap, new EmptyHashMap<String, Class<?>>());
                } else {
                    //4.4+ android.support.v4.util.SimpleArrayMap<String, Class<?>>
                }
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.installSupportV4FragmentClassCache", e);
        }
    }

    //阻止class缓存
    public static void installAndroidXFragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(androidx_fragment_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, androidx_fragment_app_Fragment_sClassMap);
            if (slCassMap != null) {
                if (slCassMap instanceof Map) {
                    RefInvoker.setField(null, FragmentClass, androidx_fragment_app_Fragment_sClassMap, new EmptyHashMap());
                } else if (slCassMap instanceof SimpleArrayMap) {
                    RefInvoker.setField(null, FragmentClass, androidx_fragment_app_Fragment_sClassMap, new EmptySimpleArrayMap());
                }
            } else {
                FragmentClass = Class.forName(androidx_fragment_app_FragmentFactory);
                slCassMap = RefInvoker.getField(null, FragmentClass, androidx_fragment_app_FragmentFactory_sClassMap);
                if (slCassMap != null) {
                    if (slCassMap instanceof Map) {
                        RefInvoker.setField(null, FragmentClass, androidx_fragment_app_FragmentFactory_sClassMap, new EmptyHashMap());
                    } else if (slCassMap instanceof SimpleArrayMap) {
                        RefInvoker.setField(null, FragmentClass, androidx_fragment_app_FragmentFactory_sClassMap, new EmptySimpleArrayMap());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.installAndroidXFragmentClassCache", e);
        }
    }

    //清理class缓存
    public static void clearFragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(android_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, android_app_Fragment_sClassMap);
            if (slCassMap != null) {
                RefInvoker.invokeMethod(slCassMap, slCassMap.getClass(), "clear", (Class[])null, (Object[])null);
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.clearFragmentClassCache", e);
        }
    }

    //清理class缓存
    public static void clearSupportV4FragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(android_support_v4_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, android_support_v4_app_Fragment_sClassMap);
            if (slCassMap != null) {
                RefInvoker.invokeMethod(slCassMap, slCassMap.getClass(), "clear", (Class[])null, (Object[])null);
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.clearSupportV4FragmentClassCache", e);
        }
    }

    //清理class缓存
    public static void clearAndroidXFragmentClassCache() {
        Class  FragmentClass = null;
        try {
            FragmentClass = Class.forName(androidx_fragment_app_Fragment);
            Object slCassMap = RefInvoker.getField(null, FragmentClass, androidx_fragment_app_Fragment_sClassMap);
            if (slCassMap == null) {
                FragmentClass = Class.forName(androidx_fragment_app_FragmentFactory);
                slCassMap = RefInvoker.getField(null, FragmentClass, androidx_fragment_app_FragmentFactory_sClassMap);
            }
            if (slCassMap != null) {
                RefInvoker.invokeMethod(slCassMap, slCassMap.getClass(), "clear", (Class[])null, (Object[])null);
            }
        } catch (ClassNotFoundException e) {
            //LogUtil.printException("CompatForFragmentClassCache.clearAndroidXFragmentClassCache", e);
        }
    }

    /**
     * 提前将fragment类的缓存添加到fragment的缓存池中
     * 注意：这里提前缓存的逻辑和框架初始化时执行的阻止缓存的逻辑从冲突的
     * @param fragmentContext
     * @param fname
     */
    public static void forceCache(Context fragmentContext, String fname) {
        try {
            //框架并不知道实际可能是什么类型，所以都试一下
            //调用下面这几个函数，会触发函数内的缓存逻辑，将fname对应的class缓存到其内部的静态map中
            android.app.Fragment.instantiate(fragmentContext, fname, null);
            RefInvoker.invokeMethod(null, android_support_v4_app_Fragment,
                "isSupportFragmentClass",new Class[]{Context.class, String.class}, new Object[]{fragmentContext, fname});
            RefInvoker.invokeMethod(null, androidx_fragment_app_Fragment,
                "isSupportFragmentClass",new Class[]{Context.class, String.class}, new Object[]{fragmentContext, fname});
            RefInvoker.invokeMethod(null, androidx_fragment_app_FragmentFactory,
                "isFragmentClass",new Class[]{ClassLoader.class, String.class}, new Object[]{fragmentContext.getClassLoader(), fname});
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
