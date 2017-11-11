package com.limpoxe.fairy.core.proxy.systemservice;

import android.os.IBinder;
import android.os.IInterface;
import android.text.TextUtils;

import com.limpoxe.fairy.core.PluginLoader;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.core.proxy.WhiteList;
import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
                try {

                    String descriptor = (String) args[0];
                    LogUtil.i("Hook服务 : " + descriptor, target.getClass().getName());

                    // 仍然可能会有一些其他服务hook不到, 比如PackageManager和ActivityManager,
                    // 是因为这些服务的binder在queryLocalInterface方法被hook之前, 已经被系统获取到了并缓存到全局静态变量中
                    // 后面再取获取这些服务的时候, 直接返回的是这些缓存, 不会调用queryLocalInterface
                    // 所以AndroidOsServiceManager应该尽可能早地执行installProxy, 以免错过hook时机

                    String className = WhiteList.getProxyImplClassName(descriptor);
                    if (TextUtils.isEmpty(className)) {
                        return null;
                    }

                    Class stubProxy = Class.forName(className, true, PluginLoader.class.getClassLoader());
                    Constructor constructor = stubProxy.getDeclaredConstructor(IBinder.class);
                    constructor.setAccessible(true);
                    IInterface proxy = (IInterface)constructor.newInstance(target);
                    SystemApiDelegate binderProxyDelegate = new SystemApiDelegate(descriptor);

                    //借此方法可以一次代理掉所有服务的remote, 而不必每个服务加一个hook
                    proxy = (IInterface)ProxyUtil.createProxy2(proxy, binderProxyDelegate);

                    return proxy;
                } catch (ClassNotFoundException e) {
                    LogUtil.printException("AndroidOsIBinder.queryLocalInterface", e);
                } catch (NoSuchMethodException e) {
                    LogUtil.printException("AndroidOsIBinder.queryLocalInterface", e);
                } catch (IllegalAccessException e) {
                    LogUtil.printException("AndroidOsIBinder.queryLocalInterface", e);
                } catch (InstantiationException e) {
                    LogUtil.printException("AndroidOsIBinder.queryLocalInterface", e);
                } catch (InvocationTargetException e) {
                    LogUtil.printException("AndroidOsIBinder.queryLocalInterface", e);
                }
            }

            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }

}
