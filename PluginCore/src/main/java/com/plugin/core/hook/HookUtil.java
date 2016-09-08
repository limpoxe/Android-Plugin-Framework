package com.plugin.core.hook;

import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.alipay.euler.andfix.Compat;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/7/14.
 */
public class HookUtil {

    private static boolean isHooked = false;

    public static boolean isHooked() {
        return isHooked;
    }

    /**
     * 此方法应该尽可能早的执行, 以免错过被hook的方法调用
     * 重要:hook成功以后,插件的getPackageName会返回插件自身的包名, 否则返回宿主的包名
     */
    public static void hook() {
        if (!isHooked && Compat.isSupport()) {
            isHooked = replaceMethod(BinderProxy.getTargetClass(), BinderProxy.getFixedMethod());
        }
    }

    private static boolean replaceMethod(Class<?> classToBeFix, Method fixedMethod) {
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
                return true;
            }
        }
        return false;
    }
}
