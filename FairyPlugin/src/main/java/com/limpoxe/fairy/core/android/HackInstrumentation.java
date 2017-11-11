package com.limpoxe.fairy.core.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackInstrumentation {
    private static final String ClassName = "android.app.Instrumentation";

    private static final String Method_execStartActivity = "execStartActivity";
    private static final String Method_execStartActivities = "execStartActivities";
    private static final String Method_execStartActivitiesAsUser = "execStartActivitiesAsUser";
    private static final String Method_execStartActivityAsCaller = "execStartActivityAsCaller";
    private static final String Method_execStartActivityFromAppTask = "execStartActivityFromAppTask";

    private Object instance;

    public HackInstrumentation(Object instance) {
        this.instance = instance;
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                                            Intent intent, int requestCode, Bundle options) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class }, new Object[] { who, contextThread, token, target,
                        intent, requestCode, options });

        return (Instrumentation.ActivityResult) result;
    }

    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target,
                                    Intent[] intents, Bundle options) {

        RefInvoker
                .invokeMethod(instance, android.app.Instrumentation.class.getName(),  Method_execStartActivities ,
                        new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent[].class,
                                Bundle.class}, new Object[]{who, contextThread, token, target, intents, options});
    }

    public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target,
                                          Intent[] intents, Bundle options, int userId) {

        RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivitiesAsUser, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent[].class, Bundle.class, int.class }, new Object[] { who, contextThread, token, target,
                        intents, options, userId });
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
                                                            Fragment target, Intent intent, int requestCode, Bundle options) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity , new Class[] { Context.class, IBinder.class, IBinder.class,
                        Fragment.class, Intent.class, int.class, Bundle.class }, new Object[] { who,
                        contextThread, token, target, intent, requestCode, options });

        return (Instrumentation.ActivityResult) result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                                            Intent intent, int requestCode, Bundle options, UserHandle user) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class, UserHandle.class }, new Object[] { who, contextThread,
                        token, target, intent, requestCode, options, user });

        return (Instrumentation.ActivityResult) result;
    }


    /////////////  Android 4.0.4及以下  ///////////////

    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class }, new Object[] { who, contextThread,
                        token, target, intent, requestCode });

        return (Instrumentation.ActivityResult) result;
    }

    public void execStartActivities(Context who, IBinder contextThread,
                                    IBinder token, Activity target, Intent[] intents) {

        RefInvoker
                .invokeMethod(instance, android.app.Instrumentation.class.getName(), Method_execStartActivities,
                        new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent[].class},
                        new Object[]{who, contextThread, token, target, intents});
    }

    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Fragment target,
            Intent intent, int requestCode) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Fragment.class,
                        Intent.class, int.class }, new Object[] { who, contextThread,
                        token, target, intent, requestCode });

        return (Instrumentation.ActivityResult) result;
    }

    /////// For Android 5.1
    public Instrumentation.ActivityResult execStartActivityAsCaller(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, int userId) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivityAsCaller, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class, int.class}, new Object[] { who, contextThread,
                        token, target, intent, requestCode, options, userId});

        return (Instrumentation.ActivityResult)result;
    }

    public void execStartActivityFromAppTask(
            Context who, IBinder contextThread, Object appTask,
            Intent intent, Bundle options) {

        try {
            RefInvoker.invokeMethod(instance, Instrumentation.class.getName(),
                    Method_execStartActivityFromAppTask, new Class[]{Context.class, IBinder.class,
                            Class.forName("android.app.IAppTask"), Intent.class, Bundle.class,},
                    new Object[]{who, contextThread, appTask, intent, options});
        } catch (ClassNotFoundException e) {
            LogUtil.printException("HackInstrumentation.execStartActivityFromAppTask", e);
        }
    }

    //7.1?
    public Instrumentation.ActivityResult execStartActivityAsCaller(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, boolean ignoreTargetSecurity,
            int userId) {

        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivityAsCaller, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class, boolean.class, int.class}, new Object[] { who, contextThread,
                        token, target, intent, requestCode, options, ignoreTargetSecurity, userId});

        return (Instrumentation.ActivityResult)result;
    }
}
