package com.limpoxe.fairy.core;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import dalvik.system.PathClassLoader;

/**
 * 插件间依赖以及so管理
 *
 * @author Administrator
 * 
 */
public class PluginClassLoader extends PathClassLoader {

	public final String pluginPackageName;

	public PluginClassLoader(String pluginPackageName, String dexPath, ClassLoader parent) {
		super(dexPath, parent);
		this.pluginPackageName = pluginPackageName;
	}

	@Override
	public String findLibrary(String name) {
		String libPath = (String) RefInvoker.invokeMethod(getParent(), getParent().getClass(), "findLibrary", new Class[]{String.class}, new Object[]{name});
		LogUtil.d("findLibrary", name, libPath);
		return libPath;
	}
}
