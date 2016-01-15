package com.plugin.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodHandler implements InvocationHandler {

    private Object mTarget = null;

    public MethodHandler(Object target) {
        this.mTarget = target;
    }

    public boolean beforeInvoke(Object target, Method method, Object[] args) {
        return false;
    }

    public Object processResult(Object result) {
        return result;
    }

    public void afterInvoke(Object target, Method method, Object[] args, Object invokeResult) {
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        boolean intercepted = beforeInvoke(mTarget, method, args);

        Object invokeResult = null;
        if (!intercepted) {
            method.setAccessible(true);
            invokeResult = method.invoke(mTarget, args);
        }

        afterInvoke(mTarget, method, args, invokeResult);

        return processResult(invokeResult);
    };
}
