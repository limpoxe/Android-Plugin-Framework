package com.plugin.core;

import com.plugin.content.LoadedPlugin;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * 插件间依赖以及so管理
 *
 * @author Administrator
 * 
 */
public class PluginClassLoader extends DexClassLoader {

	private static Hashtable<String, String> soClassloaderMapper = new Hashtable<String, String>();

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

		String soPath = super.findLibrary(name);

		String currentLoader = getClass().getName() + '@' + Integer.toHexString(hashCode());

		LogUtil.d("findLibrary", "orignal so path : " + soPath + ", current classloader : " + currentLoader);

		if (soPath != null) {
			final String soLoader = soClassloaderMapper.get(soPath);
			if (soLoader == null) {
				soClassloaderMapper.put(soPath, currentLoader);
			} else if (!currentLoader.equals(soLoader)) {
				//classloader发生了变化
				//创建so副本并返回副本路径
				StringBuilder currentSoPathBuilder = new StringBuilder(soPath);
				currentSoPathBuilder.delete(soPath.length() - 3, soPath.length());//移除.so后缀
				currentSoPathBuilder.append("_").append(Integer.toHexString(hashCode())).append(".so");
				String currentSoPath = currentSoPathBuilder.toString();
				String currentSoPathloader = soClassloaderMapper.get(currentSoPath);

				if (currentLoader.equals(currentSoPathloader)) {
					soPath = currentSoPath;
				} else {
					boolean isSuccess = FileUtil.copyFile(soPath, currentSoPath);
					if (isSuccess) {
						soPath = currentSoPath;
						soClassloaderMapper.put(soPath, currentLoader);
					}
				}
			}
		}
		LogUtil.d("findLibrary", "actually so path : " + soPath + ", current classloader : " + currentLoader);

		return soPath;
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

		//这里判断android.view 是为了解决webview的问题
		if (clazz == null && !className.startsWith("android.view")) {

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
					LoadedPlugin plugin = PluginLauncher.instance().startPlugin(dependencePluginId);

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
