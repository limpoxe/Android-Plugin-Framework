package com.limpoxe.fairy.content;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.limpoxe.fairy.util.LogUtil;

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

    public Application pluginApplication;

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

    public Class loadClassByName(String clazzName) {
        try {
            Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
            LogUtil.v("loadPluginClass Success for clazzName ", clazzName);
            return pluginClazz;
        } catch (ClassNotFoundException e) {
            LogUtil.printException("ClassNotFound " + clazzName, e);
        } catch (java.lang.IllegalAccessError illegalAccessError) {
            illegalAccessError.printStackTrace();
            throw new IllegalAccessError("出现这个异常最大的可能是插件dex和" +
                    "宿主dex包含了相同的class导致冲突, " +
                    "请检查插件的编译脚本，确保排除了所有公共依赖库的jar");
        }
        return null;
    }

}
