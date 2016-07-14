package com.plugin.core.hook;

import android.util.Log;

import com.alipay.euler.andfix.AndFix;
import com.alipay.euler.andfix.Compat;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/7/14.
 */
public class HookUtil {

    public static void replaceMethod(Class<?> classToBeFix,
                               String oldMethod, Method newMethod) {
        if (Compat.isSupport()) {
                Class<?> classToBeFixPreProcessed = AndFix.initTargetClass(classToBeFix);
                if (classToBeFixPreProcessed != null) {
                    Method src = null;
                    try {
                        src = classToBeFixPreProcessed.getDeclaredMethod(oldMethod, newMethod.getParameterTypes());
                    } catch (Exception e) {
                        Log.e("HookUtil", "replaceMethod", e.getCause());
                    }
                    if (src != null) {
                        AndFix.addReplaceMethod(src, newMethod);
                    }
                }
        }
    }
}
