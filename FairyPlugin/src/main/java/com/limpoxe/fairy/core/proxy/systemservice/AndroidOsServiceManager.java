package com.limpoxe.fairy.core.proxy.systemservice;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.view.ViewConfiguration;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackServiceManager;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import static com.limpoxe.fairy.core.proxy.ProxyUtil.createProxy;

/**
 * Created by cailiming on 16/9/15.
 */
public class AndroidOsServiceManager extends MethodProxy {

    private static HashSet<String> sCacheKeySet;
    private static HashMap<String, IBinder> sCache;

    static {
        sMethods.put("getService", new getService());
    }

    public static void installProxy() {
        LogUtil.d("安装IServiceManagerProxy");

        //for android 7.0 +
        if (Build.VERSION.SDK_INT > 23) {
            //触发初始化WindowGlobal中的静态成员变量，即触发WindowManagerGlobal.getWindowManagerService()函数被调用
            //避免7.＋的系统中对window服务代理，
            //7.+的系统代理window服务会被SELinux拒绝导致陷入死循环
            ViewConfiguration.get(FairyGlobal.getHostApplication());
            FairyGlobal.getHostApplication().getSystemService(Context.KEYGUARD_SERVICE);
            //上面两行代码都是为了触发初始化
        }

        Object androidOsServiceManagerProxy = HackServiceManager.getIServiceManager();
        Object androidOsServiceManagerProxyProxy = createProxy(androidOsServiceManagerProxy, new AndroidOsServiceManager());
        HackServiceManager.setServiceManager(androidOsServiceManagerProxyProxy);

        //干掉缓存
        sCache = HackServiceManager.getCache();
        sCacheKeySet = new HashSet<String>();
        sCacheKeySet.addAll(sCache.keySet());
        sCache.clear();

        LogUtil.d("安装完成");
    }

    public static class getService extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.i("ServiceManager.getService", args[0], invokeResult != null);
            if (ProcessUtil.isPluginProcess() && invokeResult != null) {
                IBinder binder = AndroidOsIBinder.installProxy((String)args[0], (IBinder) invokeResult);
                //0 = "package" //7.0
                //1 = "window" //7.0
                //2 = "alarm" //7.0
                if (sCacheKeySet.contains(args[0])) {
                    LogUtil.i("补回安装时干掉的缓存", args[0]);
                    //TODO 在这里可以hook window service manager
                    //AndroidViewWindowManager.installProxy()
                    sCache.put((String) args[0], binder);
                }
                return binder;
            }
            return super.afterInvoke(target, method, args, beforeInvoke, invokeResult);
        }
    }

}
