package com.limpoxe.fairy.core;

import android.os.Process;

import com.limpoxe.fairy.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cailiming on 2017/6/27.
 */

public class UncaugthExceptionWrapper implements Thread.UncaughtExceptionHandler {

    final HashMap<String, Thread.UncaughtExceptionHandler> pluginExHandlers = new HashMap<String, Thread.UncaughtExceptionHandler>();

    boolean isCalled = false;
    Thread.UncaughtExceptionHandler hostHandler = null;

    public UncaugthExceptionWrapper() {
        //可能会被创建多个实例
    }

    public void addHandler(String packageName, Thread.UncaughtExceptionHandler handler) {
        pluginExHandlers.put(packageName, handler);
    }

    public void removeHandler(String packageName) {
        pluginExHandlers.remove(packageName);
    }

    public void setHostHandler(Thread.UncaughtExceptionHandler handler) {
        this.hostHandler = handler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (isCalled) {
            LogUtil.e("这个方法只能被调用1此，防止递归，退出");
            Process.killProcess(Process.myPid());
            System.exit(10);
            return;
        }
        isCalled = true;

        //判断异常来源，是插件还是宿主
        StackTraceElement[] elements = e.getStackTrace();
        boolean isTargetHandlerFound = sendTarget(elements, t, e);

        if (!isTargetHandlerFound) {
            //尝试再识别一次
            Throwable cause = e.getCause();
            if (cause != null) {
                StackTraceElement[] ste2 = cause.getStackTrace();
                isTargetHandlerFound = sendTarget(ste2, t, e);
            }
        }

        if (!isTargetHandlerFound) {
            if (hostHandler != null) {
                LogUtil.e("未识别出此异常来源，交给宿主继续识别或处理");
                hostHandler.uncaughtException(t, e);
            } else {
                //Exception not Handled
                LogUtil.e("插件和宿主都未处理此异常，退出");
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        } else {
            LogUtil.e("插件已处理此异常，退出");
            Process.killProcess(Process.myPid());
            System.exit(10);
        }

    }

    private boolean sendTarget(StackTraceElement[] elements, Thread t, Throwable e) {
        boolean isTargetHandlerFound = false;
        if (elements != null) {
            Iterator<Map.Entry<String, Thread.UncaughtExceptionHandler>> itr = pluginExHandlers.entrySet().iterator();
            while (itr.hasNext()) {

                Map.Entry<String, Thread.UncaughtExceptionHandler> entry = itr.next();
                String packageName = entry.getKey();
                Thread.UncaughtExceptionHandler pluginHandler = entry.getValue();

                if (pluginHandler != null) {
                    for(int i = 0; i < elements.length; i++) {
                        StackTraceElement element = elements[i];
                        //异常栈里面包含插件的包名，则认为是这个插件抛出的异常
                        LogUtil.d("EEE", element.getClassName() + " packageName=" + packageName);
                        //这里只是简单的通过判断异常调用栈是否包含插件的包名来判断插件来源，不一定准确
                        //不过应该也可以覆盖大部分情况了
                        if (element.getClassName() != null && element.getClassName().startsWith(packageName)) {
                            LogUtil.e("识别出此异常来源，交给插件处理", packageName);
                            pluginHandler.uncaughtException(t, e);
                            isTargetHandlerFound = true;
                            break;
                        }
                    }
                }

                if (isTargetHandlerFound) {
                    break;
                }
            }
        }
        return isTargetHandlerFound;
    }
}
