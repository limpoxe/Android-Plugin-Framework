package com.plugin.core.app;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Handler;

import com.plugin.core.PluginAppTrace;
import com.plugin.core.PluginInstrumentionWrapper;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.util.List;

public class ActivityThread {

    private static final String android_app_ActivityThread = "android.app.ActivityThread";
    private static final String android_app_ActivityThread_currentActivityThread = "currentActivityThread";
    private static final String android_app_ActivityThread_mInstrumentation = "mInstrumentation";
    private static final String android_app_ActivityThread_getHandler = "getHandler";
    private static final String android_app_ActivityThread_installContentProviders = "installContentProviders";

    private static final String android_os_Handler_mCallback = "mCallback";

    private static Object sCurrentActivityThread;
    private static Class sClass;

    public static Class clazz() {
        if (sClass == null) {
            try {
                sClass = Class.forName(android_app_ActivityThread);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sClass;
    }

    public synchronized static Object currentActivityThread() {
        if (sCurrentActivityThread == null) {
            // 从ThreadLocal中取出来的
            LogUtil.d("从宿主程序中取出ActivityThread对象备用");
            sCurrentActivityThread = RefInvoker.invokeStaticMethod(android_app_ActivityThread,
                    android_app_ActivityThread_currentActivityThread,
                    (Class[]) null, (Object[]) null);
        }
        return sCurrentActivityThread;
    }

    public static void installContentProviders(Context context, List<ProviderInfo> providers) {
        RefInvoker.invokeMethod(currentActivityThread(),
                clazz(), android_app_ActivityThread_installContentProviders,
                new Class[]{Context.class, List.class}, new Object[]{context, providers});
    }

    public static void wrapHandler() {
        Handler handler = (Handler)RefInvoker.invokeMethod(currentActivityThread(),
                clazz(), android_app_ActivityThread_getHandler,
                (Class[]) null, (Object[]) null);
        RefInvoker.setFieldObject(handler, Handler.class.getName(), android_os_Handler_mCallback,
                new PluginAppTrace(handler));
    }
    public static void wrapInstrumentation() {
        Instrumentation originalInstrumentation = (Instrumentation) RefInvoker.getFieldObject(currentActivityThread(),
                clazz(), android_app_ActivityThread_mInstrumentation);;
        RefInvoker.setFieldObject(currentActivityThread(), clazz(),
                android_app_ActivityThread_mInstrumentation,
                new PluginInstrumentionWrapper(originalInstrumentation));
    }

}
