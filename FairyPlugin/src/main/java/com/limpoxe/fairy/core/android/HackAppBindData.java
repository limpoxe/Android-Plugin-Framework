package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackAppBindData {
    private static final String ClassName = "android.app.ActivityThread$AppBindData";

    private static final String Field_compatInfo = "compatInfo";
    private static final String Field_info = "info";

    private Object instance;

    public HackAppBindData(Object instance) {
        this.instance = instance;
    }

    public Object getInfo() {
        return RefInvoker.getField(instance, ClassName, Field_info);
    }

    public Object getCompatInfo() {
        return RefInvoker.getField(instance, ClassName, Field_compatInfo);
    }

}
