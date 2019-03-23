package com.limpoxe.fairy.util;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Copy From FreeReflection
 * https://github.com/tiann/FreeReflection
 */
public class FreeReflection {
    private static final String TAG = "FreeReflection";

    private static Object sVmRuntime;
    private static Method setHiddenApiExemptions;

    static {
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            sVmRuntime = getRuntime.invoke(null);
            // 到此处为止其实已经获得了绕开限制的方法，后续所有的受限API都可以通过上面获取到的forName、getDeclaredMethod这两个对象来获取目标类和函数(如果需要的话还可以增加getField)

            // 下面这个并不是必需的。只不过赶巧系统本身已经提供了一个豁免开关。打开开关以后，后续所有的受限API都可以直接调用了，就连上面准备的几个跳板函数都不需要了
            setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
        } catch (Throwable e) {
            Log.e(TAG, "reflect bootstrap failed:", e);
        }
    }

    public static boolean exempt(String method) {
        return exempt(new String[]{method});
    }

    public static boolean exempt(String... methods) {
        if (sVmRuntime == null || setHiddenApiExemptions == null) {
            return false;
        }

        try {
            setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{methods});
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean exemptAll() {
        //指定被豁免的方法签名字符串。所有方法签名字符串都是L开头，因此L可以豁免所有接口
        return exempt(new String[]{"L"});
    }
}
