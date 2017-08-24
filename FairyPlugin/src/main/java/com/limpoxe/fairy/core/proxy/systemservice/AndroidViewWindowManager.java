package com.limpoxe.fairy.core.proxy.systemservice;

import android.view.WindowManager;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.util.LogUtil;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/15.
 * not used
 */
public class AndroidViewWindowManager extends MethodDelegate {

    public static WindowManager installProxy(Object invokeResult) {
        LogUtil.d("安装AndroidViewWindowManagerProxy");
        WindowManager windowManager = (WindowManager)ProxyUtil.createProxy(invokeResult, new AndroidViewWindowManager());
        LogUtil.d("安装完成");
        return windowManager;
    }

    @Override
    public Object beforeInvoke(Object target, Method method, Object[] args) {
        if (args != null) {
            fixPackageName(method.getName(), args);
        }
        return super.beforeInvoke(target, method, args);
    }

    private void fixPackageName(String methodName, Object[] args) {
        if (methodName.equals("addView") || methodName.equals("updateViewLayout")) {
            for (Object object : args) {
                if (object instanceof WindowManager.LayoutParams) {
                    LogUtil.v("修正WindowManager", methodName, "方法参数中的packageName", ((WindowManager.LayoutParams)object).packageName);
                    ((WindowManager.LayoutParams)object).packageName = FairyGlobal.getHostApplication().getPackageName();
                }
            }
        }
    }

}
