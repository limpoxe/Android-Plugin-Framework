package com.limpoxe.fairy.core.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/3/11.
 */
public class AndroidAppApplication {

    public static void dispatchActivityCreated(Application application, Activity activity, Bundle savedInstanceState) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityCreated", new Class[]{Activity.class, Bundle.class}, new Object[]{activity, savedInstanceState});
    }

    public static void dispatchActivityStarted(Application application, Activity activity) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityStarted", new Class[]{Activity.class}, new Object[]{activity});
    }

    public static void dispatchActivityResumed(Application application, Activity activity) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityResumed", new Class[]{Activity.class}, new Object[]{activity});
    }

    public static void dispatchActivityPaused(Application application, Activity activity) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityPaused", new Class[]{Activity.class}, new Object[]{activity});
    }

    public static void dispatchActivityStopped(Application application, Activity activity) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityStopped", new Class[]{Activity.class}, new Object[]{activity});
    }

    public static void dispatchActivitySaveInstanceState(Application application, Activity activity, Bundle outState) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivitySaveInstanceState", new Class[]{Activity.class, Bundle.class}, new Object[]{activity, outState});
    }

    public static void dispatchActivityDestroyed(Application application, Activity activity) {
        RefInvoker.invokeMethod(application, Application.class, "dispatchActivityDestroyed", new Class[]{Activity.class}, new Object[]{activity});

    }
}
