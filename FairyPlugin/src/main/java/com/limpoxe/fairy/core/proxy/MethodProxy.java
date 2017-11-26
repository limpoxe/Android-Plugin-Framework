package com.limpoxe.fairy.core.proxy;



import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public abstract class MethodProxy extends MethodDelegate {

    /**
     *  为了省事，这里做成静态map，但是有一定的风险，如果两个系统服务定义了相同的方法名称，可能会导致proxy中命中错误的方法
     *  另外，如果同一个服务定义了同名的重载方法，可能会导致proxy中命中错误的方法
     *  不过目前还未发现这种情况。
     *  否则需要将静态map换成实例map
     */
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
