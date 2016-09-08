package com.plugin.util;

import android.content.Context;
import android.content.ContextWrapper;

import com.plugin.core.PluginLoader;

public class PackageNameUtil {
    public static Context fakeContext(Context context) {
        if (!context.getPackageName().equals(PluginLoader.getApplication().getPackageName())) {
            context = new ContextWrapper(context) {
                @Override
                public String getPackageName() {
                    return PluginLoader.getApplication().getPackageName();
                }
            };
        }
        return context;
    }
}
