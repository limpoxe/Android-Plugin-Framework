package com.limpoxe.fairy.core.android;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ActivityInfo;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackActivity extends HackContextThemeWrapper {
    private static final String ClassName = "android.app.Activity";

    private static final String Field_mActivityInfo = "mActivityInfo";
    private static final String Field_mApplication = "mApplication";
    private static final String Field_mInstrumentation = "mInstrumentation";

    public HackActivity(Object instance) {
        super(instance);
    }

    public final ActivityInfo getActivityInfo() {
        return (ActivityInfo) RefInvoker.getField(instance, ClassName, Field_mActivityInfo);
    }

    public final void setApplication(Application application) {
        RefInvoker.setField(instance, ClassName, Field_mApplication, application);
    }

    public final void setInstrumentation(Instrumentation instrumentation) {
        RefInvoker.setField(instance, ClassName, Field_mInstrumentation, instrumentation);
    }

    public final Instrumentation getInstrumentation() {
        return (Instrumentation) RefInvoker.getField(instance, ClassName, Field_mInstrumentation);
    }
}
