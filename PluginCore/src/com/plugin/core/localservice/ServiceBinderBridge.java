package com.plugin.core.localservice;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;

import com.plugin.core.PluginLoader;
import com.plugin.util.RefInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by cailiming on 16/1/1.
 */
public class ServiceBinderBridge {

    public static Object queryService(final String serviceName, String iFaceClassName) {
        try {
            ClassLoader cl = LocalServiceManager.class.getClassLoader();
            Class clientClass = LocalServiceManager.class.getClassLoader().loadClass(iFaceClassName);
            return Proxy.newProxyInstance(cl, new Class[]{clientClass},
                    new InvocationHandler() {

                        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                            Bundle bundle = PluginLoader.getApplication()
                                    .getContentResolver().call(LocalServiceBinder.buildUri(),
                                            serviceName, method.toGenericString(), wrapperParams(args));

                            if (bundle != null) {
                                return bundle.get(result);
                            }
                            return null;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static Bundle wrapperParams(Object[] args) {
        Bundle params = new Bundle();
        if (args != null && args.length >0) {
            params.putInt(method_args_count, args.length);
            for (int i = 0; i< args.length; i++) {
                putToBundle(params, String.valueOf(i), args[i]);
            }
        }
        return params;
    }

    static Object[] unwrapperParams(Bundle extras) {
        Object[] params = null;
        int maxKey = extras.getInt(ServiceBinderBridge.method_args_count);
        if (maxKey > 0) {
            params = new Object[maxKey];
            for(int i = 0; i< maxKey; i++) {
                params[i] = extras.get(String.valueOf(i));
            }
        }
        return params;
    }

    static void putToBundle(Bundle bundle, String key, Object value) {
        if (Build.VERSION.SDK_INT < 19) {

            RefInvoker.invokeMethod(bundle, android.os.Bundle.class, "unparcel", (Class[])null, (Object[])null);
            Map<String, Object> mMap = (Map<String, Object>)RefInvoker.getFieldObject(bundle, android.os.Bundle.class, "mMap");
            mMap.put(key, value);

        } else if (Build.VERSION.SDK_INT == 19) {

            RefInvoker.invokeMethod(bundle, android.os.Bundle.class, "unparcel", (Class[])null, (Object[])null);
            ArrayMap<String, Object> mMap = (ArrayMap<String, Object>)RefInvoker.getFieldObject(bundle, android.os.Bundle.class, "mMap");
            mMap.put(key, value);

        } else if(Build.VERSION.SDK_INT > 19) {

            RefInvoker.invokeMethod(bundle, android.os.BaseBundle.class, "unparcel", (Class[])null, (Object[])null);
            ArrayMap<String, Object> mMap = (ArrayMap<String, Object>)RefInvoker.getFieldObject(bundle, android.os.BaseBundle.class, "mMap");
            mMap.put(key, value);

        }

    }

    public static final String method_args_count = "method_args_count";
    public static final String result = "result";
}
