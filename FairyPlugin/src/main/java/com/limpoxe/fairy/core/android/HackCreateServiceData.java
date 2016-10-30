package com.limpoxe.fairy.core.android;

import android.content.pm.ServiceInfo;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackCreateServiceData {
    private static final String ClassName = "android.app.ActivityThread$CreateServiceData";

    private static final String Field_info = "info";

    private Object instance;

    public HackCreateServiceData(Object instance) {
        this.instance = instance;
    }

    public ServiceInfo getInfo() {
        return (ServiceInfo)RefInvoker.getField(instance, ClassName, Field_info);
    }

    public void setInfo(ServiceInfo info) {
        RefInvoker.setField(instance, ClassName, Field_info, info);
    }
}
