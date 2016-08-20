package com.plugin.core.hook;

import android.content.Context;
import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.alipay.euler.andfix.Compat;
import com.plugin.util.ProcessUtil;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/7/14.
 */
public class HookUtil {

    public static boolean isSupport(Context context) {
        if (ProcessUtil.isPluginProcess(context)) {
            return Compat.isSupport();
        } else {
            return false;
        }
    }

    /**
     * 此方法应该尽可能早的执行, 以免错过被hook的方法调用
     */
    public static void hook(Context context) {
        if (isSupport(context)) {
            replaceMethod(BinderProxy.getTargetClass(), BinderProxy.getFixedMethod());
        }
    }

    private static void replaceMethod(Class<?> classToBeFix, Method fixedMethod) {
        Class<?> classToBeFixPreProcessed = AndFix.initTargetClass(classToBeFix);
        if (classToBeFixPreProcessed != null) {
            Method methodToBeFix = null;
            try {
                methodToBeFix = classToBeFixPreProcessed.getDeclaredMethod(fixedMethod.getName(), fixedMethod.getParameterTypes());
            } catch (Exception e) {
                Log.e("HookUtil", "replaceMethod", e.getCause());
            }
            if (methodToBeFix != null) {
                AndFix.addReplaceMethod(methodToBeFix, fixedMethod);
            }
        }
    }
}
