package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

public class HackToast {

    private static final String ClassName = "android.widget.Toast";

    private static final String Field_INotificationManager_sService = "sService";

    private static final String Method_sService = "getService";

    public static Object getService() {
        return RefInvoker.invokeMethod(null, ClassName, Method_sService, (Class[])null, (Object[])null);
    }

    public static void setService(Object inotificationManager) {
        RefInvoker.setField(null, ClassName, Field_INotificationManager_sService, inotificationManager);
    }

}
