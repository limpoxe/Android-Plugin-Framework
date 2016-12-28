package com.limpoxe.fairy.core.android;

import android.app.Activity;
import android.os.Bundle;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/3/11.
 */
public class HackApplication {
    private static final String ClassName = "android.app.Application";

    private static final String Method_dispatchActivityCreated = "dispatchActivityCreated";
    private static final String Method_dispatchActivityStarted = "dispatchActivityStarted";
    private static final String Method_dispatchActivityResumed = "dispatchActivityResumed";
    private static final String Method_dispatchActivityPaused = "dispatchActivityPaused";
    private static final String Method_dispatchActivityStopped = "dispatchActivityStopped";
    private static final String Method_dispatchActivitySaveInstanceState = "dispatchActivitySaveInstanceState";
    private static final String Method_dispatchActivityDestroyed = "dispatchActivityDestroyed";

    private static final String Field_mLoadedApk = "mLoadedApk";

    private Object instance;

    public HackApplication(Object instance) {
        this.instance = instance;
    }

    public void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityCreated, new Class[]{Activity.class, Bundle.class}, new Object[]{activity, savedInstanceState});
    }

    public void dispatchActivityStarted(Activity activity) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityStarted, new Class[]{Activity.class}, new Object[]{activity});
    }

    public void dispatchActivityResumed(Activity activity) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityResumed, new Class[]{Activity.class}, new Object[]{activity});
    }

    public void dispatchActivityPaused(Activity activity) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityPaused, new Class[]{Activity.class}, new Object[]{activity});
    }

    public void dispatchActivityStopped(Activity activity) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityStopped, new Class[]{Activity.class}, new Object[]{activity});
    }

    public void dispatchActivitySaveInstanceState(Activity activity, Bundle outState) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivitySaveInstanceState, new Class[]{Activity.class, Bundle.class}, new Object[]{activity, outState});
    }

    public void dispatchActivityDestroyed(Activity activity) {
        RefInvoker.invokeMethod(instance, ClassName, Method_dispatchActivityDestroyed, new Class[]{Activity.class}, new Object[]{activity});
    }

    public Object getLoadedApk() {
        return  RefInvoker.getField(instance, ClassName, Field_mLoadedApk);
    }
}
