package com.plugin.core.systemservice;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.os.Debug;
import android.os.RemoteException;
import android.os.UserHandle;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppIActivityManager extends MethodProxy {

    private AndroidAppIActivityManager() {
    }

    public static void installProxy(ActivityManager manager) {
        Object androidAppIActivityManagerStubProxy = RefInvoker.invokeStaticMethod("android.app.ActivityManagerNative", "getDefault", (Class[])null, (Object[])null);
        Object androidAppIActivityManagerStubProxyProxy = ProxyUtil.createProxy(androidAppIActivityManagerStubProxy, new AndroidAppIActivityManager());
        RefInvoker.setStaticOjbect("android.app.ActivityManagerNative", "gDefault", androidAppIActivityManagerStubProxyProxy);
    }

    static {
        sMethods.put("getRunningAppProcesses", new getRunningAppProcesses());
        sMethods.put("killBackgroundProcesses", new killBackgroundProcesses());
        sMethods.put("getRunningServices", new getRunningServices());
    }

    //public List<RunningAppProcessInfo> getRunningAppProcesses()
    public static class getRunningAppProcesses extends MethodDelegate {
        @Override
        public boolean beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }

    //public void killBackgroundProcesses(String packageName)
    public static class killBackgroundProcesses extends MethodDelegate {
        @Override
        public boolean beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }

    //public List<RunningServiceInfo> getRunningServices(int maxNum)
    public static class getRunningServices extends MethodDelegate {
        @Override
        public boolean beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO
            return super.beforeInvoke(target, method, args);
        }
    }


}
