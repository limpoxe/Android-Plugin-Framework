package com.limpoxe.fairy.core;

import dalvik.system.PathClassLoader;

/**
 * 插件间依赖以及so管理
 *
 * @author Administrator
 * 
 */
public class PluginClassLoader extends PathClassLoader {

	public PluginClassLoader(String dexPath, ClassLoader parent) {
		super(dexPath, parent);
	}
}
