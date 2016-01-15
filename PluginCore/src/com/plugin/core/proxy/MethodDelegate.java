package com.plugin.core.proxy;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodDelegate {

    public boolean beforeInvoke(Object target, Method method, Object[] args) {
        return false;
    }

    public Object afterInvoke(Object target, Method method, Object[] args, Object invokeResult) {
        return invokeResult;
    }

}
