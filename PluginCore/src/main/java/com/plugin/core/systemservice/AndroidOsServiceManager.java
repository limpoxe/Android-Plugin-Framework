package com.plugin.core.systemservice;

import android.os.IBinder;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.util.LogUtil;
import com.plugin.util.ProcessUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import static com.plugin.core.proxy.ProxyUtil.createProxy;

/**
 * Created by cailiming on 16/9/15.
 */
public class AndroidOsServiceManager extends MethodProxy {

    private static HashSet<String> sCacheKeySet;
    private static HashMap<String, IBinder> sCache;

    static {
        sMethods.put("getService", new getService());
    }

    public static void installProxy() {
        LogUtil.d("安装IServiceManagerProxy");
        Object androidOsServiceManagerProxy = RefInvoker.invokeStaticMethod("android.os.ServiceManager", "getIServiceManager", (Class[])null, (Object[])null);
        Object androidOsServiceManagerProxyProxy = createProxy(androidOsServiceManagerProxy, new AndroidOsServiceManager());
        RefInvoker.setStaticOjbect("android.os.ServiceManager", "sServiceManager", androidOsServiceManagerProxyProxy);

        //干掉缓存
        sCache = (HashMap<String, IBinder>)RefInvoker.getFieldObject(null, "android.os.ServiceManager", "sCache");
        sCacheKeySet = new HashSet<String>();
        sCacheKeySet.addAll(sCache.keySet());
        sCache.clear();

        LogUtil.d("安装完成");
    }

    public static class getService extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.d("afterInvoke", method.getName(), args[0]);
            if (ProcessUtil.isPluginProcess()) {
                IBinder binder = AndroidOsIBinder.installProxy((IBinder) invokeResult);
                //补回安装时干掉的缓存
                if (sCacheKeySet.contains(args[0])) {
                    sCache.put((String) args[0], binder);
                }
                return binder;
            }
            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }

}
