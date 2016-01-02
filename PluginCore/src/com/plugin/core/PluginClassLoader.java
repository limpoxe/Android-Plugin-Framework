package com.plugin.core;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;

import dalvik.system.DexClassLoader;

/**
 * 为了支持插件间依赖，增加此类。
 * 
 * @author Administrator
 * 
 */
public class PluginClassLoader extends DexClassLoader {

	private String[] dependencies;

	public PluginClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, String[] dependencies) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		this.dependencies = dependencies;
	}

	@Override
	public String findLibrary(String name) {
		LogUtil.d("findLibrary", name);
		return super.findLibrary(name);
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		Class<?> clazz = null;
		ClassNotFoundException suppressed = null;
		try {
			clazz = super.findClass(className);
		} catch (ClassNotFoundException e) {
			suppressed = e;
		}

		if (clazz == null && !className.startsWith("android.view")) {//这里判断android.view 是为了解决webview的问题
			if (dependencies != null) {
				for (String dependencePluginId: dependencies) {
					PluginDescriptor pd = PluginLoader.initPluginByPluginId(dependencePluginId);
					if (pd != null) {
						try {
							clazz = pd.getPluginClassLoader().loadClass(className);
						} catch (ClassNotFoundException e) {
						}
						if (clazz != null) {
							break;
						}
					} else {
						LogUtil.e("PluginClassLoader", "未找到插件", dependencePluginId, className);
					}
				}
			}
		}

		if (clazz == null && suppressed != null) {
			throw suppressed;
		}

		return clazz;
	}
}
