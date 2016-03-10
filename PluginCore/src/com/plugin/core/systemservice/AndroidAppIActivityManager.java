package com.plugin.core.systemservice;

import android.content.Intent;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.NotificationHelper;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppIActivityManager extends MethodProxy {

    private AndroidAppIActivityManager() {
    }

    public static void installProxy() {
        LogUtil.d("安装ActivityManagerProxy");
        Object androidAppActivityManagerProxy = RefInvoker.invokeStaticMethod("android.app.ActivityManagerNative", "getDefault", (Class[])null, (Object[])null);
        Object androidAppIActivityManagerStubProxyProxy = ProxyUtil.createProxy(androidAppActivityManagerProxy, new AndroidAppIActivityManager());
        Object singleton = RefInvoker.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");
        //如果是IActivityManager
        if (singleton.getClass().isAssignableFrom(androidAppIActivityManagerStubProxyProxy.getClass())) {
            RefInvoker.setStaticOjbect("android.app.ActivityManagerNative", "gDefault", androidAppIActivityManagerStubProxyProxy);
        } else {//否则是包装过的单例
            RefInvoker.setFieldObject(singleton, "android.util.Singleton", "mInstance", androidAppIActivityManagerStubProxyProxy);
        }
        LogUtil.d("安装完成");
    }

    static {
        sMethods.put("getRunningAppProcesses", new getRunningAppProcesses());
        sMethods.put("killBackgroundProcesses", new killBackgroundProcesses());
        sMethods.put("getServices", new getServices());
        sMethods.put("getIntentSender", new getIntentSender());
    }

    //public List<RunningAppProcessInfo> getRunningAppProcesses()
    public static class getRunningAppProcesses extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO 需要时再说
            return super.beforeInvoke(target, method, args);
        }
    }

    //public void killBackgroundProcesses(String packageName)
    public static class killBackgroundProcesses extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO 需要时再说
            return super.beforeInvoke(target, method, args);
        }
    }

    //public List<RunningServiceInfo> getRunningServices(int maxNum)
    public static class getServices extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            //TODO 需要时再说
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getIntentSender extends MethodDelegate {

        public static final int INTENT_SENDER_BROADCAST = 1;
        public static final int INTENT_SENDER_ACTIVITY = 2;
        public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;
        public static final int INTENT_SENDER_SERVICE = 4;

        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.e("beforeInvoke", method.getName());
            int type = (int)args[0];
            if (type != INTENT_SENDER_ACTIVITY_RESULT) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && args[i].getClass().isAssignableFrom(Intent[].class)) {
                        Intent[] intents = (Intent[])args[i];
                        if (type == INTENT_SENDER_BROADCAST) {
                            type = PluginDescriptor.BROADCAST;
                        } else if (type == INTENT_SENDER_ACTIVITY) {
                            type = PluginDescriptor.ACTIVITY;
                        } else if (type == INTENT_SENDER_SERVICE) {
                            type = PluginDescriptor.SERVICE;
                        }
                        for(int j = 0; j < intents.length; j++) {
                            intents[j] =  NotificationHelper.resolveNotificationIntent(intents[j], type);
                        }
                        break;
                    }
                }
            }

            return super.beforeInvoke(target, method, args);
        }
    }
}
