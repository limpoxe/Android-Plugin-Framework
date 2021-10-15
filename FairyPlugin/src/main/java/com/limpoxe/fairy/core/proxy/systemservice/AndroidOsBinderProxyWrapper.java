package com.limpoxe.fairy.core.proxy.systemservice;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import com.limpoxe.fairy.core.PluginLoader;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.core.proxy.WhiteList;
import com.limpoxe.fairy.util.LogUtil;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AndroidOsBinderProxyWrapper implements IBinder {

    IBinder mReal;

    public AndroidOsBinderProxyWrapper(IBinder real) {
        mReal = real;
    }

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mReal.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return mReal.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mReal.isBinderAlive();
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        IInterface invokeResult = mReal.queryLocalInterface(descriptor);
        if (invokeResult == null) {//为空表示不是服务侧
            invokeResult = hookQueryLocalInterface(descriptor, this);
        }
        return invokeResult;
    }

    @Override
    public void dump(FileDescriptor fd, String[] args)
        throws RemoteException {
        mReal.dump(fd, args);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void dumpAsync(FileDescriptor fd, String[] args)
        throws RemoteException {
        mReal.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags)
        throws RemoteException {
        return mReal.transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags)
        throws RemoteException {
        mReal.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return mReal.unlinkToDeath(recipient, flags);
    }

    static IInterface hookQueryLocalInterface(final String descriptor, IBinder binder) {
        try {
            LogUtil.i("Hook服务 : " + descriptor, binder.getClass().getName());

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
            IInterface proxy = (IInterface)constructor.newInstance(binder);
            SystemApiDelegate binderProxyDelegate = new SystemApiDelegate(descriptor);

            //借此方法可以一次代理掉所有服务的remote, 而不必每个服务加一个hook
            proxy = (IInterface)ProxyUtil.createProxy2(proxy, binderProxyDelegate);

            return proxy;
        } catch (ClassNotFoundException e) {
            LogUtil.printException("hookQueryLocalInterface", e);
        } catch (NoSuchMethodException e) {
            LogUtil.printException("hookQueryLocalInterface", e);
        } catch (IllegalAccessException e) {
            LogUtil.printException("hookQueryLocalInterface", e);
        } catch (InstantiationException e) {
            LogUtil.printException("hookQueryLocalInterface", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else if(cause instanceof Error) {
                throw (Error)cause;
            } else {
                throw new RuntimeException("hookQueryLocalInterface", cause);
            }
        }
        return null;
    }
}
