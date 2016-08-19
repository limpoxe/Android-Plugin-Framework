package com.plugin.core.proxy;



import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodProxy extends MethodDelegate {

    public static Map<String, MethodDelegate> sMethods = new HashMap<String, MethodDelegate>(5);

    protected MethodDelegate findMethodDelegate(String methodName, Object[] args) {
        return sMethods.get(methodName);
    }

    @Override
    public Object beforeInvoke(Object target, Method method, Object[] args) {
        MethodDelegate deleate = findMethodDelegate(method.getName(), args);
        if (deleate != null) {
            return deleate.beforeInvoke(target, method, args);
        }
        return super.beforeInvoke(target, method, args);
    }

    @Override
    public Object afterInvoke(Object target, Method method, Object[] args, Object before, Object invokeResult) {
        MethodDelegate deleate = findMethodDelegate(method.getName(), args);
        if (deleate != null) {
            return deleate.afterInvoke(target, method, args, before, invokeResult);
        }
        return super.afterInvoke(target, method, args, before, invokeResult);
    }


}
