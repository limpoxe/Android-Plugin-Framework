package com.limpoxe.fairy.core.android;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.ArrayMap;

import com.limpoxe.fairy.util.RefInvoker;

import java.io.File;

/**
 * Created by cailiming on 16/10/25.
 */

public class HackContextImpl {

    private static final String ClassName = "android.app.ContextImpl";

    private static final String Field_mReceiverRestrictedContext = "mReceiverRestrictedContext";
    private static final String Field_mSharedPrefsPaths = "mSharedPrefsPaths";
    private static final String Field_mPreferencesDir = "mPreferencesDir";
    private static final String Field_mMainThread = "mMainThread";
    private static final String Field_mBasePackageName = "mBasePackageName";
    private static final String Field_mOpPackageName = "mOpPackageName";
    private static final String Field_sSharedPrefs = "sSharedPrefs";

    private static final String Method_getOuterContext = "getOuterContext";
    private static final String Method_setOuterContext = "setOuterContext";
    private static final String Method_getImpl = "getImpl";
    private static final String Method_getReceiverRestrictedContext = "getReceiverRestrictedContext";

    private Object instance;

    public HackContextImpl(Object instance) {
        this.instance = instance;
    }

    public void setReceiverRestrictedContext(Object value) {
        RefInvoker.setField(instance, ClassName, Field_mReceiverRestrictedContext, value);
    }

    public ContextWrapper getReceiverRestrictedContext() {
        return (ContextWrapper)RefInvoker.invokeMethod(instance, ClassName, Method_getReceiverRestrictedContext, null, null);
    }

    public ArrayMap<String, File> getSharedPrefsPaths() {
        return (ArrayMap<String, File>)RefInvoker.getField(instance, ClassName, Field_mSharedPrefsPaths);
    }

    public void setPreferencesDir(Object value) {
        RefInvoker.setField(instance, ClassName, Field_mPreferencesDir, value);
    }

    public File getPreferencesDir() {
        return (File)RefInvoker.getField(instance, ClassName, Field_mPreferencesDir);
    }

    public static Object getSharedPrefs() {
        return RefInvoker.getField(null, ClassName, Field_sSharedPrefs);
    }

    public Object getMainThread() {
        return RefInvoker.getField(instance, ClassName, Field_mMainThread);
    }

    public Context getOuterContext() {
        return (Context)RefInvoker.invokeMethod(instance, ClassName, Method_getOuterContext, null, null);
    }

    public void setOuterContext(Object paramValues) {
        RefInvoker.invokeMethod(instance, ClassName, Method_setOuterContext, new Class[]{Context.class}, new Object[]{paramValues});
    }

    public static boolean instanceOf(Object object) {
        return object.getClass().getName().equals(ClassName);
    }

    public static Object getImpl(Object paramValues) {
        return RefInvoker.invokeMethod(null, ClassName, Method_getImpl, new Class[]{Context.class}, new Object[]{paramValues});
    }

    public void setBasePackageName(Object value) {
        RefInvoker.setField(instance, ClassName, Field_mBasePackageName, value);
    }

    public void setOpPackageName(Object value) {
        if (Build.VERSION.SDK_INT > 18) {
            RefInvoker.setField(instance, ClassName, Field_mOpPackageName, value);
        }
    }

}
