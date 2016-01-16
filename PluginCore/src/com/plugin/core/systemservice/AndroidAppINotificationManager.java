package com.plugin.core.systemservice;

import android.app.NotificationManager;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppINotificationManager extends MethodProxy {

    private AndroidAppINotificationManager() {
    }

    public static void installProxy(NotificationManager manager) {
        Object androidAppINotificationStubProxy = RefInvoker.invokeStaticMethod(NotificationManager.class.getName(), "getService", (Class[])null, (Object[])null);
        Object androidAppINotificationStubProxyProxy = ProxyUtil.createProxy(androidAppINotificationStubProxy, new AndroidAppINotificationManager());
        RefInvoker.setStaticOjbect(NotificationManager.class.getName(), "sService", androidAppINotificationStubProxyProxy);
    }

    static {
        sMethods.put("enqueueNotification", new enqueueNotification());
        sMethods.put("enqueueNotificationWithTag", new enqueueNotificationWithTag());
        sMethods.put("enqueueNotificationWithTagPriority", new enqueueNotificationWithTagPriority());
    }

    public static class enqueueNotification extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTag extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTagPriority extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }

}
