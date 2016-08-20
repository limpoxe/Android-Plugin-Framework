package com.plugin.core.systemservice;

import android.app.NotificationManager;
import android.widget.Toast;

import com.plugin.core.PluginLoader;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidWidgetToast extends MethodProxy {

    static {
        sMethods.put("enqueueToast", new enqueueToast());
        sMethods.put("cancelToast", new cancelToast());
    }

    public static void installProxy() {
        LogUtil.d("安装NotificationManagerProxy");
        Object androidAppINotificationStubProxy = RefInvoker.invokeStaticMethod(Toast.class.getName(), "getService", (Class[])null, (Object[])null);
        Object androidAppINotificationStubProxyProxy = ProxyUtil.createProxy(androidAppINotificationStubProxy, new AndroidWidgetToast());
        RefInvoker.setStaticOjbect(NotificationManager.class.getName(), "sService", androidAppINotificationStubProxyProxy);
        LogUtil.d("安装完成");
    }

    public static class enqueueToast extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class cancelToast extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
            return super.beforeInvoke(target, method, args);
        }
    }

}
