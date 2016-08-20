package com.plugin.core.systemservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.PluginShadowService;
import com.plugin.core.app.ActivityThread;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.PendingIntentHelper;
import com.plugin.util.ProcessUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppIActivityManager extends MethodProxy {

    static {
        sMethods.put("getRunningAppProcesses", new getRunningAppProcesses());
        sMethods.put("killBackgroundProcesses", new killBackgroundProcesses());
        sMethods.put("getServices", new getServices());
        sMethods.put("getIntentSender", new getIntentSender());
        sMethods.put("overridePendingTransition", new overridePendingTransition());
        sMethods.put("serviceDoneExecuting", new serviceDoneExecuting());
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

    //public List<RunningAppProcessInfo> getRunningAppProcesses()
    public static class getRunningAppProcesses extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.e("afterInvoke", method.getName());
            //由于插件运行在插件进程中，这里需要欺骗插件，让插件的中判断进程的逻辑以为当前是在主进程中运行
            //但是这会导致插件框架也无法判断当前的进程了，因此框架中判断插件进程的方法一定要在安装ActivityManager代理之前执行并记住状态
            //同时要保证主进程能正确判断进程。
            //这里不会导致无限递归，因为ProcessUtil.isPluginProcess方法内部有缓存，再安装ActivityManager代理之前已经执行并缓存了
            if (ProcessUtil.isPluginProcess()) {
                List<ActivityManager.RunningAppProcessInfo> result = (List<ActivityManager.RunningAppProcessInfo>)invokeResult;
                for (ActivityManager.RunningAppProcessInfo appProcess : result) {
                    if (appProcess.pid == android.os.Process.myPid()) {
                        appProcess.processName = PluginLoader.getApplication().getPackageName();
                        break;
                    }
                }
            }

            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
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
            LogUtil.e("beforeInvoke", method.getName(), args[1]);
            int type = (int)args[0];
            args[1] = PluginLoader.getApplication().getPackageName();
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
                            intents[j] =  PendingIntentHelper.resolvePendingIntent(intents[j], type);
                        }
                        break;
                    }
                }
            }

            return super.beforeInvoke(target, method, args);
        }
    }

    public static class overridePendingTransition extends MethodDelegate {
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            if (ProcessUtil.isPluginProcess()) {
                //屏蔽插件进程的Activity转场动画
                String packageName = (String)args[1];
                args[2] = 0;
                args[3] = 0;
            }
            return null;
        }
    }

    public static class serviceDoneExecuting extends MethodDelegate {
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            if (ProcessUtil.isPluginProcess()) {
                for (Object obj: args) {
                    if (obj instanceof IBinder) {
                        Map<IBinder, Service> services = ActivityThread.getAllServices();
                        Service service = services.get(obj);
                        if (service instanceof PluginShadowService) {
                            if (((PluginShadowService) service).realService != null) {
                                services.put((IBinder) obj, ((PluginShadowService) service).realService);
                            } else {
                                throw new IllegalStateException("unable to create service");
                            }
                        }
                        break;
                    }
                }
            }
            return null;
        }
    }

}
