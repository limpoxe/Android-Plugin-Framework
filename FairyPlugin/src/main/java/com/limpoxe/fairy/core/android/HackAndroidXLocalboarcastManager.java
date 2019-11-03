package com.limpoxe.fairy.core.android;

import android.content.BroadcastReceiver;

import com.limpoxe.fairy.util.RefInvoker;

import java.util.HashMap;

/**
 * Created by cailiming on 19/11/3.
 */

public class HackAndroidXLocalboarcastManager {

    private static final String ClassName = "androidx.localbroadcastmanager.content.LocalBroadcastManager";

    private static final String Field_mInstance = "mInstance";
    private static final String Field_mReceivers = "mReceivers";

    private static final String Method_unregisterReceiver = "unregisterReceiver";

    private Object instance ;

    public HackAndroidXLocalboarcastManager(Object instance) {
        this.instance = instance;
    }

    public static Object getInstance() {
        return RefInvoker.getField(null, ClassName, Field_mInstance);
    }

    public HashMap getReceivers() {
        return (HashMap)RefInvoker.getField(instance, ClassName, Field_mReceivers);
    }

    public void unregisterReceiver(BroadcastReceiver item) {
        RefInvoker.invokeMethod(instance, ClassName, Method_unregisterReceiver, new Class[]{BroadcastReceiver.class}, new Object[]{item});

    }

}
