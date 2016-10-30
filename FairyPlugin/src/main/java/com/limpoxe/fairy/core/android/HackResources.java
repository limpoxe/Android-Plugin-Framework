package com.limpoxe.fairy.core.android;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackResources {
    private static final String ClassName = "android.content.res.Resources";

    private static final String Method_selectDefaultTheme = "selectDefaultTheme";

    public static Integer selectDefaultTheme(int mThemeResource,
                                            int targetSdkVersion) {
       return (Integer) RefInvoker.invokeMethod(null, ClassName, Method_selectDefaultTheme, new Class[]{
                int.class, int.class}, new Object[]{mThemeResource, targetSdkVersion});
    }
}
