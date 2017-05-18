package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 17/5/18.
 */

public class HackActivityManager {

    private static final String ClassName = "android.app.ActivityManager";

    private static final String Field_IActivityManagerSingleton = "IActivityManagerSingleton";

    public static Object getIActivityManagerSingleton() {
        return RefInvoker.getField(null, ClassName, Field_IActivityManagerSingleton);
    }

    public static Object setIActivityManagerSingleton(Object activityManagerSingleton) {
       return RefInvoker.getField(null, ClassName, Field_IActivityManagerSingleton);
    }

}
