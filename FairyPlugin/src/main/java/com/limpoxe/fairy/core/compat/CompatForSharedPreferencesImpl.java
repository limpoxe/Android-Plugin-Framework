package com.limpoxe.fairy.core.compat;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by cailiming on 16/4/14.
 */
public class CompatForSharedPreferencesImpl {

    private static Class SharedPreferencesImpl;
    private static boolean has2 = true;
    private static boolean has3 = true;
    private static Constructor SharedPreferencesImpl_Constructor_2;
    private static Constructor SharedPreferencesImpl_Constructor_3;

    public static Object newSharedPreferencesImpl(File prefsFile, int mode, String packageName) {
        if (SharedPreferencesImpl == null) {
            try {
                SharedPreferencesImpl = Class.forName("android.app.SharedPreferencesImpl");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }


        if (has2 && SharedPreferencesImpl_Constructor_2 == null) {
            try {
                SharedPreferencesImpl_Constructor_2 = SharedPreferencesImpl.getDeclaredConstructor(File.class, int.class);
                SharedPreferencesImpl_Constructor_2.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                has2 = false;
            }
        }

        if (SharedPreferencesImpl_Constructor_2 != null) {
            try {
                return SharedPreferencesImpl_Constructor_2.newInstance(prefsFile, mode);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        //compat for moto 4.0.4
        if (has3 && SharedPreferencesImpl_Constructor_3 == null) {
            try {
                SharedPreferencesImpl_Constructor_3 = SharedPreferencesImpl.getDeclaredConstructor(File.class, int.class, String.class);
                SharedPreferencesImpl_Constructor_3.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                has3 = false;
            }
        }

        if (SharedPreferencesImpl_Constructor_3 != null) {
            try {
                return SharedPreferencesImpl_Constructor_3.newInstance(prefsFile, mode, packageName);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
