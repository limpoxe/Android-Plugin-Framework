package com.limpoxe.fairy.core.proxy.systemservice;

import android.view.WindowManager;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginLoader;
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
        if (args != null) {
            fixPackageName(method.getName(), args);
        }
        return super.beforeInvoke(target, method, args);
    }

    private void fixPackageName(String methodName, Object[] args) {
        for (Object object : args) {
            if (object != null && object instanceof WindowManager.LayoutParams) {

                WindowManager.LayoutParams attr = ((WindowManager.LayoutParams)object);

                if (attr.packageName != null && !attr.packageName.equals(PluginLoader.getApplication().getPackageName())) {

                    //尝试读取插件, 注意, 这个方法调用会触发ContentProvider调用
                    PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(attr.packageName);
                    if(pd != null) {
                        LogUtil.v("修正System api", methodName, "WindowManager.LayoutParams.packageName参数为宿主包名", attr.packageName);
                        // 参数传的是插件包名, 修正为宿主包名
                        attr.packageName = PluginLoader.getApplication().getPackageName();
                        // 这里或许需要break,提高效率
                    }
                }
                ((WindowManager.LayoutParams)object).packageName = PluginLoader.getApplication().getPackageName();
            }
        }
    }

}
