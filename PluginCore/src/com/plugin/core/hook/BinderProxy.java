package com.plugin.core.hook;

import android.os.IBinder;
import android.os.IInterface;

import com.plugin.core.PluginLoader;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/7/14.
 * 实现Serializable是为了防混淆
 */
public class BinderProxy implements Serializable {

    //此方法应该尽可能早的执行, 以免错过被hook的方法调用
    public static void hook() {
        HookUtil.replaceMethod(BinderProxy.getTargetClass(), BinderProxy.getFixedMethod());
    }

    public static Class getTargetClass() {
        try {
            return Class.forName("android.os.BinderProxy");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getFixedMethod() {
        try {
            return BinderProxy.class.getDeclaredMethod("queryLocalInterface", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    //不可混淆此方法
    public IInterface queryLocalInterface(String descriptor) {
        try {

            Object thisObject = this;
            Class thisClass = thisObject.getClass();

            LogUtil.e(descriptor, thisClass.getName());

            Class stubProxy = null;

            //TODO
            // 通常情况下,如果是通过编译命令生成的接口, 类名如下
            // 接口类全名 : descriptor
            // 接口服务端侧实现类基类全名 : descriptor.Stub
            // 接口客户端侧代理类全名称 : descriptor.Stub.Proxy
            // 但是也有特殊情况,不是通过命令生成,而是自行实现的,这种情况就需要做白名单
            // 例如
            //      android.content.IContentProvider ---> descriptor
            //      android.content.ContentProviderNative ---> descriptor.Stub
            //      android.content.ContentProviderProxy ---> descriptor.Stub.Proxy
            // 不过contentprovider这个例子比较特殊, 正好不能hook, 否则会造成递归, 因为在被hook的实现里面,调用的Contentprovider查询插件信息

            //其他:
            //android.view.accessibility.IAccessibilityInteractionConnectionCallback
            //com.android.internal.view.IInputMethodSession
            //com.android.internal.view.IInputMethodManager
            //com.android.internal.view.IInputMethodClient
            //android.hardware.input.IInputManager
            //android.view.IWindowSession
            //android.os.IPowerManager
            //android.app.IUiModeManager
            //com.android.internal.telephony.ITelephony
            //com.android.internal.telephony.ITelephonyRegistry

            // 不过仍然会有一些其他服务hook不到, 是因为服务的remote对象,
            // 在执行replaceMethod方法前已经被获取到了, 即queryLocalInterface这个方法被hook之前已经被执行

            if("android.content.IContentProvider".equals(descriptor)) {
                return null;
            }
            stubProxy = Class.forName(descriptor + "$Stub$Proxy", true, PluginLoader.class.getClassLoader());
            Constructor constructor = stubProxy.getDeclaredConstructor(IBinder.class);
            constructor.setAccessible(true);
            IInterface proxy = (IInterface)constructor.newInstance(this);

            // 这里的写法看起来有点诡异是andfix引起的.否则会出现classNotFound问题
            Constructor t = Class.forName("com.plugin.core.hook.BinderProxyDelegate",
                    true, ProxyUtil.class.getClassLoader()).getDeclaredConstructor(String.class);
            Object binderProxyDelegate = t.newInstance(descriptor);

            //借此方法可以代理掉所有服务的remote, 而不必每个服务加一个hook
            proxy = (IInterface)ProxyUtil.createProxy2(proxy, binderProxyDelegate);

            return proxy;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 用下面的方法实现拦截会更好一点,
    // 可惜的是不支持4.4以下,
    // 因为4.4以下的BinderProxy全是native方法,
    // andfix不支持修改native方法
    /**public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        //Binder.checkParcel(this, code, data, "Unreasonably large binder buffer");
        Object thisProxy = this;
        Object result = RefInvoker.invokeMethod(thisProxy, "android.os.BinderProxy", "transactNative",
                new Class[]{int.class, Parcel.class, Parcel.class, int.class},
                new Object[]{ code,  data,  reply,  flags});
        return result == null? false: (Boolean) result;
    }*/
}
