package com.plugin.util;

import android.app.Application;

import com.plugin.core.PluginLoader;
import com.plugin.core.PluginClassLoader;


public class ClassLoaderUtil {

	/**
	 * 如果插件中不包含service、receiver和contentprovider，是不需要替换classloader的
	 */
	public static void hackClassLoaderIfNeeded() {
		Object mLoadedApk = RefInvoker.getFieldObject(PluginLoader.getApplicatoin(), Application.class.getName(),
				"mLoadedApk");
		ClassLoader originalLoader = (ClassLoader) RefInvoker.getFieldObject(mLoadedApk, "android.app.LoadedApk",
				"mClassLoader");
		if (!(originalLoader instanceof PluginClassLoader)) {
			PluginClassLoader newLoader = new PluginClassLoader("", PluginLoader.getApplicatoin()
					.getCacheDir().getAbsolutePath(),
					PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath(), originalLoader);
			RefInvoker.setFieldObject(mLoadedApk, "android.app.LoadedApk", "mClassLoader", newLoader);
		}
	}

}
