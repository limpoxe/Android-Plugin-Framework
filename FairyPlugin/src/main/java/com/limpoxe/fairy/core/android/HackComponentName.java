package com.limpoxe.fairy.core.android;

import android.content.ComponentName;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackComponentName extends HackContextThemeWrapper {
    private static final String ClassName = ComponentName.class.getName();
    private static final String Field_mPackage = "mPackage";
    private static final String Field_mClass = "mClass";

    public HackComponentName(Object instance) {
        super(instance);
    }

    public final void setPackageName(String packageName) {
        RefInvoker.setField(instance, ClassName, Field_mPackage, packageName);
    }

    public final void setClassName(String className) {
        RefInvoker.setField(instance, ClassName, Field_mClass, className);
    }
}
