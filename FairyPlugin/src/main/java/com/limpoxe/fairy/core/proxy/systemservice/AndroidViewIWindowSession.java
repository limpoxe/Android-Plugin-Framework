package com.limpoxe.fairy.core.proxy.systemservice;

import android.view.WindowManager;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 * not used
 */
public class AndroidViewIWindowSession extends MethodDelegate {

    public static Object installProxy(Object invokeResult) {
        LogUtil.d("安装AndroidViewIWindowSessionProxy");
        Object iWindowSessionProxy = ProxyUtil.createProxy(invokeResult, new AndroidViewIWindowSession());
        LogUtil.d("安装完成");
        return iWindowSessionProxy;
    }

    @Override
    public Object beforeInvoke(Object target, Method method, Object[] args) {
        LogUtil.v("beforeInvoke", method.getName());
        if (args != null) {
            fixPackageName(method.getName(), args);
        }
        return super.beforeInvoke(target, method, args);
    }

    private void fixPackageName(String methodName, Object[] args) {
        for (Object object : args) {
            if (object != null && object instanceof WindowManager.LayoutParams) {

                WindowManager.LayoutParams params = ((WindowManager.LayoutParams)object);

                if (params.packageName != null && !params.packageName.equals(FairyGlobal.getApplication().getPackageName())) {

                    //尝试读取插件, 注意, 这个方法调用会触发ContentProvider调用
                    PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(params.packageName);
                    if(pluginDescriptor != null) {
                        LogUtil.v("修正System api", methodName, params.packageName);
                        //这里修正packageName会引起弹PopupWindow时发生WindowManager异常，
                        //TODO 此处暂不修正，似乎无需修正，原因待查
                        //params.packageName = PluginLoader.getApplication().getPackageName();
                    }
                }
            }
        }
    }

}
