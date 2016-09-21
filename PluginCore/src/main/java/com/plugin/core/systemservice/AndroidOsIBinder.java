package com.plugin.core.systemservice;

import android.os.IBinder;
import android.os.IInterface;

import com.plugin.core.PluginLoader;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;

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

    public static IBinder installProxy(IBinder invokeResult) {
        LogUtil.d("安装AndroidOsIBinderProxy");
        IBinder result = (IBinder)invokeResult;
        IBinder resultProxy = (IBinder)ProxyUtil.createProxy(result, new AndroidOsIBinder());
        LogUtil.d("安装完成");
        return resultProxy;
    }

    public static class queryLocalInterface extends MethodDelegate {
        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            if (invokeResult != null) {
            }
            try {

                String descriptor = (String)args[0];
                LogUtil.e(descriptor, target.getClass().getName());

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
                //android.view.accessibility.IAccessibilityManager
                //android.view.IAssetAtlas
                //android.view.IGraphicsStats
                //android.view.IWindowManager
                //android.view.IWindowSession
                //com.android.internal.view.IInputMethodSession
                //com.android.internal.view.IInputMethodManager
                //com.android.internal.view.IInputMethodClient
                //com.android.internal.telephony.ITelephony
                //com.android.internal.telephony.ITelephonyRegistry
                //com.android.internal.telephony.ISub
                //com.android.internal.app.IBatteryStats
                //android.app.IUiModeManager
                //android.app.IWallpaperManager
                //android.bluetooth.IBluetoothManager
                //android.content.IBulkCursor
                //android.content.IContentService
                //android.hardware.input.IInputManager
                //android.hardware.usb.IUsbManager
                //android.net.wifi.IWifiManager
                //android.os.IBatteryPropertiesRegistrar
                //android.os.IMessenger
                //android.os.IPowerManager
                //android.os.IUserManager
                //android.vrsystem.IVRSystemService
                //android.webkit.IWebViewUpdateService

                //com.huawei.permission.IHoldService

                // 不过仍然可能会有一些其他服务hook不到, 是因为服务的remote对象
                // 在AndroidOsServiceManager方法被hook前已经被获取到了, 即queryLocalInterface这个方法被hook之前已经被执行
                // 所以AndroidOsServiceManager应该尽可能早地执行installProxy

                Class stubProxy = null;

                if ("android.content.IContentProvider".equals(descriptor)) {

                    return null;

                } else if ("IMountService".equals(descriptor)) {

                    stubProxy = Class.forName("android.os.storage.IMountService$Stub$Proxy", true, PluginLoader.class.getClassLoader());

                } else if ("android.content.IBulkCursor".equals(descriptor)) {

                    stubProxy = Class.forName("android.database.BulkCursorProxy", true, PluginLoader.class.getClassLoader());

                } else {
                    //默认
                    stubProxy = Class.forName(descriptor + "$Stub$Proxy", true, PluginLoader.class.getClassLoader());
                }
                Constructor constructor = stubProxy.getDeclaredConstructor(IBinder.class);
                constructor.setAccessible(true);
                IInterface proxy = (IInterface)constructor.newInstance(target);
                BinderProxyDelegate binderProxyDelegate = new BinderProxyDelegate(descriptor);

                //借此方法可以一次代理掉所有服务的remote, 而不必每个服务加一个hook
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

            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }

}
