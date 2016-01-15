package com.plugin.core.proxy;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public interface MethodDelegate {

    public boolean beforeInvoke(Object target, Method method, Object[] args);

    public Object afterInvoke(Object target, Method method, Object[] args, Object invokeResult);

}
