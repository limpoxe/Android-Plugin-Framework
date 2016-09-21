package com.plugin.core.systemservice;

import android.os.IBinder;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.util.LogUtil;
import com.plugin.util.ProcessUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;

import static com.plugin.core.proxy.ProxyUtil.createProxy;

/**
 * Created by cailiming on 16/9/15.
 */
public class AndroidOsServiceManager extends MethodProxy {

    static {
        sMethods.put("getService", new getService());
    }

    public static void installProxy() {
        LogUtil.d("安装IServiceManagerProxy");
        Object androidOsServiceManagerProxy = RefInvoker.invokeStaticMethod("android.os.ServiceManager", "getIServiceManager", (Class[])null, (Object[])null);
        Object androidOsServiceManagerProxyProxy = createProxy(androidOsServiceManagerProxy, new AndroidOsServiceManager());
        RefInvoker.setStaticOjbect("android.os.ServiceManager", "sServiceManager", androidOsServiceManagerProxyProxy);
        LogUtil.d("安装完成");
    }

    public static class getService extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.e("afterInvoke", method.getName());
            if (ProcessUtil.isPluginProcess()) {
               return AndroidOsIBinder.installProxy((IBinder) invokeResult);
            }
            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }

}
