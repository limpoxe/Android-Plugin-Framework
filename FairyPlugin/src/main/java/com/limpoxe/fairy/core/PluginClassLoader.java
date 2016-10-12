package com.limpoxe.fairy.core;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.util.FileUtil;
import com.limpoxe.fairy.util.LogUtil;

import java.io.File;
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

		final String thisLoader = getClass().getName() + '@' + Integer.toHexString(hashCode());
		final String soPath = super.findLibrary(name);

		LogUtil.v("findLibrary", "orignal so path : " + soPath + ", current classloader : " + thisLoader);

		if (soPath != null) {
			final String soLoader = soClassloaderMapper.get(soPath);
			if (soLoader == null || soLoader.equals(thisLoader)) {
				soClassloaderMapper.put(soPath, thisLoader);
				LogUtil.v("findLibrary", "acturely so path : " + soPath + ", current classloader : " + thisLoader);
				return soPath;
			} else {
				//classloader发生了变化, 创建so副本并返回副本路径, 限制最多10个副本
				for (int i = 1; i < 5; i++) {

					String soPathOfCopyN = tryPath(soPath, i);
					String soLoaderOfCopyN = soClassloaderMapper.get(soPathOfCopyN);

					if (thisLoader.equals(soLoaderOfCopyN)) {
						LogUtil.v("findLibrary", "acturely so path : " + soPathOfCopyN + ", current classloader : " + thisLoader);
						return soPathOfCopyN;
					} else if (soLoaderOfCopyN == null) {
						if(!new File(soPathOfCopyN).exists()) {
							boolean isSuccess = FileUtil.copyFile(soPath, soPathOfCopyN);
							if (isSuccess) {
								soClassloaderMapper.put(soPathOfCopyN, thisLoader);
								LogUtil.v("findLibrary", "acturely so path : " + soPathOfCopyN + ", current classloader : " + thisLoader);
								return soPathOfCopyN;
							} else {
								return null;
							}
						} else {
							soClassloaderMapper.put(soPathOfCopyN, thisLoader);
							LogUtil.v("findLibrary", "acturely so path : " + soPathOfCopyN + ", current classloader : " + thisLoader);
							return soPathOfCopyN;
						}
					}
				}
				LogUtil.e("findLibrary", "最多创建5个副本...");
			}
		}
		return null;
	}

	private String tryPath(String orignalPath, int i) {
		StringBuilder soPathBuilder = new StringBuilder(orignalPath);
		soPathBuilder.delete(orignalPath.length() - 3, orignalPath.length());//移除.so后缀
		soPathBuilder.append("_").append(i).append(".so");
		return soPathBuilder.toString();
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

					//被依赖的插件可能尚未初始化，确保使用前已经初始化
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
