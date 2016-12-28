package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackActivityManagerNative {

    private static final String ClassName = "android.app.ActivityManagerNative";

    private static final String Method_getDefault = "getDefault";

    private static final String Field_gDefault = "gDefault";

    public static Object getDefault() {
        return RefInvoker.invokeMethod(null, ClassName, Method_getDefault, (Class[])null, (Object[])null);
    }

    public static Object getGDefault() {
       return RefInvoker.getField(null, ClassName, Field_gDefault);
    }

    public static void setGDefault(Object gDefault) {
        RefInvoker.setField(null, ClassName, Field_gDefault, gDefault);
    }
}
