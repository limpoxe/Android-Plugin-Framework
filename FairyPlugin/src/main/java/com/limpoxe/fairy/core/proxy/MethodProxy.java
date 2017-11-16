package com.limpoxe.fairy.core.proxy;



import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodProxy extends MethodDelegate {

    public static final Map<String, MethodDelegate> sMethods = new HashMap<String, MethodDelegate>(5);

    private MethodDelegate findMethodDelegate(String methodName, Object[] args) {
        return sMethods.get(methodName);
    }

    @Override
    public Object beforeInvoke(Object target, Method method, Object[] args) {
        String methodName = method.getName();
        MethodDelegate delegate = findMethodDelegate(methodName, args);
        if (delegate != null) {
            LogUtil.v("beforeInvoke", methodName);
            return delegate.beforeInvoke(target, method, args);
        }
        return super.beforeInvoke(target, method, args);
    }

    @Override
    public Object afterInvoke(Object target, Method method, Object[] args, Object before, Object invokeResult) {
        String methodName = method.getName();
        MethodDelegate deleate = findMethodDelegate(methodName, args);
        if (deleate != null) {
            LogUtil.v("afterInvoke", methodName);
            return deleate.afterInvoke(target, method, args, before, invokeResult);
        }
        return super.afterInvoke(target, method, args, before, invokeResult);
    }


}
