package com.limpoxe.fairy.core.compat;

import com.limpoxe.fairy.util.RefInvoker;

import java.util.HashMap;

/**
 * for supportv7
 */
public class CompatForFragmentClassCache {

    private static final String android_support_v4_app_Fragment = "android.support.v4.app.Fragment";
    private static final String android_support_v4_app_Fragment_sClassMap = "sClassMap";

    private static final String android_app_Fragment = "android.app.Fragment";
    private static final String android_app_Fragment_sClassMap = "sClassMap";

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
                    //4.4+ SimpleArrayMap<String, Class<?>>
                    //RefInvoker.setField(null, FragmentClass, android_support_v4_app_Fragment_sClassMap, );
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

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
                    //4.4+ ArrayMap<String, Class<?>>
                    //RefInvoker.setField(null, FragmentClass, android_support_v4_app_Fragment_sClassMap, );
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
