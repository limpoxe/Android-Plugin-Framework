package com.limpoxe.fairy.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodHandler extends MethodDelegate implements InvocationHandler {

    private final Object mTarget;

    private final MethodDelegate mDelegate;

    public MethodHandler(Object target, MethodDelegate delegate) {
        this.mTarget = target;
        this.mDelegate = delegate;
    }

    @Override
    public Object beforeInvoke(Object target, Method method, Object[] args) {
        return mDelegate.beforeInvoke(target, method, args);
    }

    @Override
    public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
        return mDelegate.afterInvoke(target, method, args, beforeInvoke, invokeResult);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Object before = beforeInvoke(mTarget, method, args);

        Object invokeResult = null;
        if (before == null) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            invokeResult = method.invoke(mTarget, args);
        }

        return afterInvoke(mTarget, method, args, before, invokeResult);
    }

}
