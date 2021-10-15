package com.limpoxe.fairy.core.proxy.systemservice;

import android.os.IBinder;

import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidOsIBinder extends MethodProxy {

    static {
        sMethods.put("queryLocalInterface", new queryLocalInterface());
    }

    public static IBinder installProxy(String serviceName, IBinder invokeResult) {
        LogUtil.d("安装AndroidOsIBinderProxy For " + serviceName);
        IBinder result = (IBinder)invokeResult;
        IBinder resultProxy = (IBinder)ProxyUtil.createProxy(result, new AndroidOsIBinder());
        LogUtil.d("安装完成");
        return resultProxy;
    }

    public static class queryLocalInterface extends MethodDelegate {
        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            if (invokeResult == null) {//为空表示不是服务侧
                invokeResult = AndroidOsBinderProxyWrapper.hookQueryLocalInterface((String)args[0], (IBinder)target);
                if (invokeResult != null) {
                    return invokeResult;
                }
            }
            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }
}
