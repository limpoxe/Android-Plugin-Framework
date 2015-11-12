package com.plugin.core.proxy;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.plugin.core.PluginIntentResolver;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 由于service的特殊性，采用欺骗的方式加载插件Service时只能同时存在一个实例
 * <p/>
 * 所以这里仍然通过service代理的方式来支持多Service
 *
 * @author cailiming
 */
public class PluginProxyService extends Service {

    private final HashMap<String, Service> serviceMap = new HashMap<String, Service>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {

            String action = intent.getAction();
            LogUtil.d("onStartCommand action", action);

            if (action != null) {

                String[] targetClassName = action.split(PluginIntentResolver.SERVICE_START_ACTION_IN_PLUGIN);

                String clazzName = targetClassName[0];

                LogUtil.d("tagertServiceClass ", clazzName);

                if (clazzName != null) {

                    Service service = serviceMap.get(clazzName);
                    Class clazz = null;
                    if (service == null) {
                        clazz = PluginLoader.loadPluginClassByName(clazzName);
                        intent.setExtrasClassLoader(clazz.getClassLoader());
                        try {
                            service = (Service) clazz.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        attach(service);
                        service.onCreate();
                        serviceMap.put(clazzName, service);
                    } else {
                        clazz = service.getClass();
                        intent.setExtrasClassLoader(clazz.getClassLoader());
                    }

                    //由于之前intent被修改过 这里再吧Intent还原到原始的intent
                    if (targetClassName.length > 1) {
                        intent.setAction(targetClassName[1]);
                    } else {
                        intent.setAction(null);
                    }
                    return service.onStartCommand(intent, flags, startId);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * bind
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null && intent.getAction() != null) {

            String action = intent.getAction();
            LogUtil.d("onBind action", action);
            String[] targetClassName = action.split(PluginIntentResolver.SERVICE_BIND_ACTION_IN_PLUGIN);

            String clazzName = targetClassName[0];

            LogUtil.d("tagertServiceClass ", clazzName);
            if (clazzName != null) {

                Service service = serviceMap.get(clazzName);
                Class clazz = null;
                if (service == null) {
                    clazz = PluginLoader.loadPluginClassByName(clazzName);
                    intent.setExtrasClassLoader(clazz.getClassLoader());
                    try {
                        service = (Service) clazz.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    attach(service);
                    service.onCreate();
                    serviceMap.put(clazzName, service);
                } else {
                    clazz = service.getClass();
                    intent.setExtrasClassLoader(clazz.getClassLoader());
                }

                //由于之前intent被修改过 这里再吧Intent还原到原始的intent
                if (targetClassName.length > 1) {
                    intent.setAction(targetClassName[1]);
                } else {
                    intent.setAction(null);
                }

                IBinder iBinder = service.onBind(intent);
                //调用目标onBind后需要把intent Action 再还原回修改后，保证conn回调
                intent.setAction(action);
                return iBinder;
            }
        }
        return null;
    }

    @Override
    public void onLowMemory() {
        Iterator<Service> itr = serviceMap.values().iterator();
        while (itr.hasNext()) {
            try {
                itr.next().onLowMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if (Build.VERSION.SDK_INT >= 14) {
            Iterator<Service> itr = serviceMap.values().iterator();
            while (itr.hasNext()) {
                try {
                    itr.next().onTrimMemory(level);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Iterator<Service> itr = serviceMap.values().iterator();
        while (itr.hasNext()) {
            return itr.next().onUnbind(intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Iterator<Service> itr = serviceMap.values().iterator();
        while (itr.hasNext()) {
            itr.next().onRebind(intent);
        }
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Iterator<Service> itr = serviceMap.values().iterator();
        while (itr.hasNext()) {
            try {
                itr.next().onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        serviceMap.clear();
    }

    private void attach(Service service) {
        RefInvoker.invokeMethod(service, ContextWrapper.class.getName(), "attachBaseContext",
                new Class[]{Context.class},
                new Object[]{PluginLoader.getDefaultPluginContext(service.getClass())});
        set(service, "mClassName");
        set(service, "mToken");
        set(service, "mApplication");
        set(service, "mActivityManager");
        set(service, "mStartCompatibility");
    }

    private void set(Service service, String name) {
        LogUtil.d("attach " + name);
        Object obj = RefInvoker.getFieldObject(this, Service.class.getName(), name);
        if (obj != null) {
            RefInvoker.setFieldObject(service, Service.class.getName(), name, obj);
        }
    }

}
