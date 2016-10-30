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
        LogUtil.v("beforeInvoke", method.getName());
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
                        LogUtil.v("修正System api", methodName, attr.packageName, ((WindowManager.LayoutParams)object).packageName);

                        // 这里修正packageName会引起弹PopupWindow时发生WindowManager异常，
                        // 看起来是资源问题引起
                        //#00  pc 0002057a  /system/lib/libandroidfw.so (android::ResTable::getBagLocked(unsigned int, android::ResTable::bag_entry const**, unsigned int*) const+153)

                        //TODO 此处暂不修正，原因待查, 可能需要clone，不能直接修改
                        //attr.packageName = PluginLoader.getApplication().getPackageName();
                        //((WindowManager.LayoutParams)object).packageName = PluginLoader.getApplication().getPackageName();
                    }
                }
            }
        }
    }

}
