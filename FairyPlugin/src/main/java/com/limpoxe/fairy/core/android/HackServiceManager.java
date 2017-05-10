package com.limpoxe.fairy.core.android;

import android.os.IBinder;

import com.limpoxe.fairy.util.RefInvoker;

import java.util.HashMap;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackServiceManager {
    private static final String ClassName = "android.os.ServiceManager";

    private static final String Field_sServiceManager = "sServiceManager";
    private static final String Field_sCache = "sCache";

    private static final String Method_getIServiceManager = "getIServiceManager";
    private static final String Method_getService = "getService";

    public static Object getIServiceManager() {
        return RefInvoker.invokeMethod(null, ClassName, Method_getIServiceManager, (Class[])null, (Object[])null);
    }

    public static void setServiceManager(Object serviceManager) {
        RefInvoker.setField(null, ClassName, Field_sServiceManager, serviceManager);
    }

    public static HashMap<String, IBinder> getCache() {
        return (HashMap<String, IBinder>)RefInvoker.getField(null, ClassName, Field_sCache);
    }

    public static IBinder getService(String name) {
        return (IBinder)RefInvoker.invokeMethod(null, ClassName, Method_getService, new Class[]{String.class}, new Object[]{name});
    }
}
