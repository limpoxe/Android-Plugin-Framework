package com.limpoxe.fairy.core.android;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackContextThemeWrapper extends HackContextWrapper {
    private static final String ClassName = "android.view.ContextThemeWrapper";

    private static final String Field_mResources = "mResources";
    private static final String Field_mTheme = "mTheme";

    private static final String Method_attachBaseContext = "attachBaseContext";

    public HackContextThemeWrapper(Object instance) {
       super(instance);
    }

    public final void attachBaseContext(Object paramValues) {
        RefInvoker.invokeMethod(instance, ClassName, Method_attachBaseContext, new Class[]{Context.class}, new Object[]{paramValues});
    }

    public final void setResources(Resources resources) {
        if (Build.VERSION.SDK_INT > 16) {
            RefInvoker.setField(instance, ClassName, Field_mResources, resources);
        }
    }

    public final void setTheme(Resources.Theme theme) {
        RefInvoker.setField(instance, ClassName, Field_mTheme, theme);
    }
}
