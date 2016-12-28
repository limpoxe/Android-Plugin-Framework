package com.limpoxe.fairy.core.android;

import android.content.Context;
import android.view.LayoutInflater;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackWindow {
    private static final String ClassName = "android.view.Window";

    private static final String Field_mContext = "mContext";
    private static final String Field_mWindowStyle = "mWindowStyle";
    private static final String Field_mLayoutInflater = "mLayoutInflater";

    private Object instance;

    public HackWindow(Object instance) {
        this.instance = instance;
    }

    public void setContext(Context context) {
        RefInvoker.setField(instance, ClassName, Field_mContext, context);
    }

    public void setWindowStyle(Object style) {
        RefInvoker.setField(instance, ClassName, Field_mWindowStyle, style);
    }

    public void setLayoutInflater(String className, LayoutInflater layoutInflater) {
        RefInvoker.setField(instance, className, Field_mLayoutInflater, layoutInflater);
    }

}
