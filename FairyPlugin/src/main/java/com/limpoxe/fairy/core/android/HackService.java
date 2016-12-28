package com.limpoxe.fairy.core.android;

import android.app.Application;
import android.content.Context;
import android.os.IBinder;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackService extends HackContextWrapper {
    private static final String ClassName = "android.app.Service";

    private static final String Field_mApplication = "mApplication";
    private static final String Field_mClassName = "mClassName";
    private static final String Field_mThread = "mThread";
    private static final String Field_mToken = "mToken";
    private static final String Field_mActivityManager = "mActivityManager";
    private static final String Field_mStartCompatibility = "mStartCompatibility";

    private static final String Method_attach = "attach";

    public HackService(Object instance) {
        super(instance);
    }

    public void attach(Context mBaseContext,
                       Object mThread,
                       String mClassName,
                       IBinder mToken,
                       Application mApplication,
                       Object mActivityManager) {

        RefInvoker.invokeMethod(instance, ClassName, Method_attach,
                new Class[]{Context.class,
                        HackActivityThread.clazz(), String.class, IBinder.class,
                        Application.class, Object.class},
                new Object[]{mBaseContext, mThread, mClassName, mToken,
                        mApplication, mActivityManager});

    }

    public void setApplication(Application application) {
        RefInvoker.setField(instance, ClassName, Field_mApplication, application);
    }

    public void setClassName(String name) {
        RefInvoker.setField(instance, ClassName, Field_mClassName, name);
    }

    public String getClassName() {
        return (String)RefInvoker.getField(instance, ClassName, Field_mClassName);
    }

    public Object getThread() {
        return RefInvoker.getField(instance, ClassName, Field_mThread);
    }

    public IBinder getToken() {
        return (IBinder)RefInvoker.getField(instance, ClassName, Field_mToken);
    }

    public Object getActivityManager() {
        return RefInvoker.getField(instance, ClassName, Field_mActivityManager);
    }

    public Boolean getStartCompatibility() {
        return (Boolean)RefInvoker.getField(instance, ClassName, Field_mStartCompatibility);
    }

    public void setStartCompatibility(Boolean compat) {
        RefInvoker.setField(instance, ClassName, Field_mStartCompatibility, compat);
    }
}
