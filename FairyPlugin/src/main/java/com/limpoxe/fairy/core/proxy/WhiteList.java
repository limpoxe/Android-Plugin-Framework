package com.limpoxe.fairy.core.proxy;

import java.util.HashMap;

/**
 * Created by cailiming on 2017/3/28.
 */

public class WhiteList {

    public static final HashMap<String, String> sInterfaceList = new HashMap<String, String>();

    static {
        // 通常情况下,如果是通过编译命令生成的接口, 类名如下
        // 接口类全名 : descriptor
        // 接口服务端侧实现类基类全名 : descriptor.Stub
        // 接口客户端侧代理类全名称 : descriptor.Stub.Proxy
        // 但是也有特殊情况,不是通过命令生成,而是自行实现的,这种情况就需要做白名单
        // 例如:
        //      android.content.IContentProvider ---> descriptor
        //      android.content.ContentProviderNative ---> descriptor.Stub
        //      android.content.ContentProviderProxy ---> descriptor.Stub.Proxy
        // 不过contentprovider这个例子比较特殊, 正好不能hook, 否则会造成递归, 因为在被hook的实现里面,调用的Contentprovider查询插件信息

        add("android.content.IContentProvider",          null);//不需要hook
        add("com.android.internal.telephony.ITelephony", null);//不需要hook
        add("IMountService",                             "android.os.storage.IMountService$Stub$Proxy");//命令规则非默认
        add("android.content.IBulkCursor",               "android.database.BulkCursorProxy");//命令规则非默认

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
        //android.security.IKeystoreService
        //android.vrsystem.IVRSystemService
        //android.webkit.IWebViewUpdateService
        //com.huawei.permission.IHoldService
        //com.android.internal.app.IAppOpsService
        //android.net.IConnectivityManager
    }

    public static void add(String descriptor, String implClassName) {
        sInterfaceList.put(descriptor, implClassName);
    }

    public static String getProxyImplClassName(String descriptor) {
        if (sInterfaceList.containsKey(descriptor)) {
            return sInterfaceList.get(descriptor);
        } else {
            //默认命名规则
            return descriptor + "$Stub$Proxy";
        }
    }

}
