package com.plugin.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodHandler implements InvocationHandler, MethodDelegate {

    private Object mTarget = null;

    private MethodDelegate mDelegate;

    public MethodHandler(Object target, MethodDelegate delegate) {
        this.mTarget = target;
        this.mDelegate = delegate;
    }

    @Override
    public boolean beforeInvoke(Object target, Method method, Object[] args) {
        return mDelegate.beforeInvoke(target, method, args);
    }

    @Override
    public Object afterInvoke(Object target, Method method, Object[] args, Object invokeResult) {
        return mDelegate.afterInvoke(target, method, args, invokeResult);
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        boolean intercepted = beforeInvoke(mTarget, method, args);

        Object invokeResult = null;
        if (!intercepted) {
            method.setAccessible(true);
            invokeResult = method.invoke(mTarget, args);
        }

        return afterInvoke(mTarget, method, args, invokeResult);
    }

}
