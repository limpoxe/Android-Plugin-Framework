package com.limpoxe.fairy.core.android;

import android.content.pm.ApplicationInfo;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackRemoteViews {
    private static final String ClassName = "android.widget.RemoteViews";

    private static final String Field_mLayoutId = "mLayoutId";
    private static final String Field_mApplication = "mApplication";
    private static final String Field_mPackage = "mPackage";

    private Object instance;

    public HackRemoteViews(Object instance) {
        this.instance = instance;
    }

    public Integer getLayoutId() {
        return (Integer)RefInvoker.getField(instance, ClassName, Field_mLayoutId);
    }

    public void setLayoutId(int layoutId) {
        RefInvoker.setField(instance, ClassName, Field_mLayoutId, new Integer(layoutId));
    }

    public void setApplicationInfo(ApplicationInfo info) {
        RefInvoker.setField(instance, ClassName, Field_mApplication, info);
    }

    public void setPackage(String packageName) {
        RefInvoker.setField(instance, ClassName, Field_mPackage, packageName);
    }
}
