package com.plugin.core.systemservice;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public class PluginNotificationManager extends MethodProxy {

    static {
        sMethods.put("enqueueNotification", new enqueueNotification());
        sMethods.put("enqueueNotificationWithTag", new enqueueNotificationWithTag());
    }

    @Override
    public MethodDelegate findMethodDelegate(String methodName, Object[] args){
        return null;
    }

    public static class enqueueNotification extends MethodDelegate {
        @Override
        public boolean beforeInvoke(Object target, Method method, Object[] args) {
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTag extends MethodDelegate {
        @Override
        public boolean beforeInvoke(Object target, Method method, Object[] args) {
            return super.beforeInvoke(target, method, args);
        }
    }
}
