package com.limpoxe.fairy.core.proxy;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodDelegate {

    public Object beforeInvoke(Object target, Method method, Object[] args) {
        return null;
    }

    public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
        if (beforeInvoke != null) {
            return beforeInvoke;
        }
        return invokeResult;
    }

}
