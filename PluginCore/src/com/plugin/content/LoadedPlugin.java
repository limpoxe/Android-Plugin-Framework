package com.plugin.content;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import dalvik.system.DexClassLoader;

/**
 * Created by cailiming on 16/3/9.
 *
 */
public class LoadedPlugin {

    public final DexClassLoader pluginClassLoader;
    public final Context pluginContext;
    public final Resources pluginResource;

    public final String pluginPackageName;
    public final String pluginSourceDir;

    public LoadedPlugin(String packageName,
                        String pluginSourceDir,
                        Context pluginContext,
                        DexClassLoader pluginClassLoader) {
        this.pluginPackageName = packageName;
        this.pluginSourceDir = pluginSourceDir;
        this.pluginContext = pluginContext;
        this.pluginClassLoader = pluginClassLoader;
        this.pluginResource = pluginContext.getResources();
    }

    public Application pluginApplication;
}
