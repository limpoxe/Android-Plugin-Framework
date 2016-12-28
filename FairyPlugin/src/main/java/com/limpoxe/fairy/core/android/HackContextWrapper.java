package com.limpoxe.fairy.core.android;

import android.content.Context;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackContextWrapper {
    private static final String ClassName = "android.content.ContextWrapper";

    private static final String Field_mBase = "mBase";

    protected Object instance;

    public HackContextWrapper(Object instance) {
        this.instance = instance;
    }

    public final Context getBase() {
        return (Context)RefInvoker.getField(instance, ClassName, Field_mBase);
    }

    public final void setBase(Context context) {
        RefInvoker.setField(instance, ClassName, Field_mBase, context);
    }
}
