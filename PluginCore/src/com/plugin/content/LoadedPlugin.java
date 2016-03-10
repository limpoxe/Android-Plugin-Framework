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

    public final Application pluginApplication;
    public final DexClassLoader pluginClassLoader;
    public final Context pluginContext;
    public final Resources pluginResource;

    public final String pluginPackageName;
    public final String pluginSourceDir;

    public LoadedPlugin(String packageName,
                        String pluginSourceDir,
                        Application pluginApplication,
                        Context pluginContext,
                        DexClassLoader pluginClassLoader) {
        this.pluginPackageName = packageName;
        this.pluginSourceDir = pluginSourceDir;
        this.pluginApplication = pluginApplication;
        this.pluginContext = pluginContext;
        this.pluginClassLoader = pluginClassLoader;
        this.pluginResource = pluginContext.getResources();
    }

    public Context getPluginContext() {
        return pluginContext;
    }

    public DexClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public Application getPluginApplication() {
        return pluginApplication;
    }

    public Resources getPluginResource() {
        return pluginResource;
    }

    public String loadString(String idString) {
        if (idString != null && idString.startsWith("@") && idString.length() == 9) {
            String idHex = idString.replace("@", "");
            try {
                int id = Integer.parseInt(idHex, 16);
                //此时context可能还没有初始化
                return pluginResource.getString(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return idString;
    }

}
