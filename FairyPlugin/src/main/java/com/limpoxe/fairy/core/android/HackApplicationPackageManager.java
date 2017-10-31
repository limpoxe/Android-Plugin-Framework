package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackApplicationPackageManager {
    private static final String ClassName = "android.app.ApplicationPackageManager";

    private static final String Field_mPM = "mPM";

    private Object instance;

    public HackApplicationPackageManager(Object instance) {
        this.instance = instance;
    }

    public void setPM(Object pm) {
        RefInvoker.setField(instance, ClassName, Field_mPM, pm);
    }
}
