package com.limpoxe.fairy.core.android;

import android.content.ContentProvider;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

public class HackActivityThreadProviderClientRecord {

    private static final String ClassName = "android.app.ActivityThread$ProviderClientRecord";

    private static final String Field_mProvider = "mLocalProvider";

    private Object instance;

    public HackActivityThreadProviderClientRecord(Object instance) {
        this.instance = instance;
    }

    public static Class clazz() {
        try {
            return RefInvoker.forName(ClassName);
        } catch (ClassNotFoundException e) {
            LogUtil.printException("HackActivityThreadProviderClientRecord.clazz", e);
        }
        return null;
    }

    public ContentProvider getProvider() {
        Object o = RefInvoker.getField(instance, ClassName, Field_mProvider);
        if (o instanceof ContentProvider) {//maybe ContentProviderProxy
            return (ContentProvider) o;
        }
        return null;
    }
}
