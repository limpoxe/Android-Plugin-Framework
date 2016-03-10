package com.plugin.core;

import com.plugin.content.LoadedPlugin;
import com.plugin.content.PluginRuntime;
import com.plugin.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * 为了支持插件间依赖，增加此类。
 * 
 * @author Administrator
 * 
 */
public class PluginClassLoader extends DexClassLoader {

	private String[] dependencies;
	private List<DexClassLoader> multiDexClassLoaderList;

	public PluginClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent,
							 String[] dependencies, List<String> multiDexList) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		this.dependencies = dependencies;

		if (multiDexList != null) {
			if (multiDexClassLoaderList == null) {
				multiDexClassLoaderList = new ArrayList<DexClassLoader>(multiDexList.size());
				for(String path: multiDexList) {
					multiDexClassLoaderList.add(new DexClassLoader(path, optimizedDirectory, libraryPath, parent));
				}
			}
		}
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

			if (multiDexClassLoaderList != null) {
				for(DexClassLoader dexLoader : multiDexClassLoaderList) {
					try {
						clazz = dexLoader.loadClass(className);
					} catch (ClassNotFoundException e) {
					}
					if (clazz != null) {
						break;
					}
				}
			}

			if (clazz == null && dependencies != null) {
				for (String dependencePluginId: dependencies) {

					//插件可能尚未初始化，确保使用前已经初始化
					LoadedPlugin plugin = PluginRuntime.instance().startPlugin(dependencePluginId);

					if (plugin != null) {
						try {
							clazz = plugin.pluginClassLoader.loadClass(className);
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
