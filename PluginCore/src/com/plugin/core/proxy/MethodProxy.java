package com.plugin.core.proxy;


import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodProxy implements MethodDelegate {

    @Override
    public boolean beforeInvoke(Object target, Method method, Object[] args) {
        MethodDelegate deleate = findMethod(method.getName(), args);
        if (deleate == null) {
            return false;
        }
        return deleate.beforeInvoke(target, method, args);
    }

    @Override
    public Object afterInvoke(Object target, Method method, Object[] args, Object invokeResult) {
        MethodDelegate deleate = findMethod(method.getName(), args);
        if (deleate == null) {
            return invokeResult;
        }
        return deleate.afterInvoke(target, method, args, invokeResult);
    }

    public abstract MethodDelegate findMethod(String methodName, Object[] args);

}
