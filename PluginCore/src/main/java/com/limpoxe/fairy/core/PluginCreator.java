package com.limpoxe.fairy.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.File;
import java.util.List;

import dalvik.system.DexClassLoader;

public class PluginCreator {

	private PluginCreator() {
	}

	/**
	 * 根据插件apk文件，创建插件dex的classloader
	 * 
	 * @param absolutePluginApkPath
	 *            插件apk文件路径
	 * @return
	 */
	public static DexClassLoader createPluginClassLoader(String absolutePluginApkPath, boolean isStandalone,
														 String[] dependences, List<String> pluginApkMultDexPath) {

		String apkParentDir = new File(absolutePluginApkPath).getParent();

		File optDir = new File(apkParentDir, "dalvik-cache");
		optDir.mkdirs();

		File libDir = new File(apkParentDir, "lib");
		libDir.mkdirs();

		if (!isStandalone) {//非独立插件
			return new PluginClassLoader(
					absolutePluginApkPath,
					optDir.getAbsolutePath(),
					libDir.getAbsolutePath(),
					PluginLoader.class.getClassLoader(),//宿主classloader
					dependences,//插件依赖的插件
					null);
		} else {//独立插件
			return new PluginClassLoader(
					absolutePluginApkPath,
					optDir.getAbsolutePath(),
					libDir.getAbsolutePath(),
					/*
			         * In theory this should be the "system" class loader; in practice we
			         * don't use that and can happily (and more efficiently) use the
			         * bootstrap class loader.
			         */
					ClassLoader.getSystemClassLoader().getParent(),//系统classloader
					null,//独立插件无依赖
					pluginApkMultDexPath);
		}

	}

	/**
	 * 根据插件apk文件，创建插件资源文件，同时绑定宿主程序的资源，这样就可以在插件中使用宿主程序的资源。
	 *
	 * @return
	 */
	public static Resources createPluginResource(String mainApkPath, Resources mainRes, PluginDescriptor pluginDescriptor) {
		String absolutePluginApkPath = pluginDescriptor.getInstalledPath();
		if (new File(absolutePluginApkPath).exists()) {
			boolean isStandalone = pluginDescriptor.isStandalone();
			String[] dependencies = pluginDescriptor.getDependencies();

			try {
				String[] assetPaths = buildAssetPath(isStandalone, mainApkPath,
						absolutePluginApkPath, dependencies);
				AssetManager assetMgr = AssetManager.class.newInstance();
				RefInvoker.invokeMethod(assetMgr, AssetManager.class.getName(), "addAssetPaths",
						new Class[] { String[].class }, new Object[] { assetPaths });

				Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(),
						mainRes.getConfiguration(), pluginDescriptor);

				return pluginRes;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LogUtil.e("插件文件不存在", absolutePluginApkPath);
		}

		return null;
	}

	private static String[] buildAssetPath(boolean isStandalone, String app, String plugin, String[] dependencies) {
		dependencies = null;//暂不支持资源多级依赖, 会导致插件难以维护
		String[] assetPaths = new String[isStandalone ? 1 : (2 + (dependencies==null?0:dependencies.length))];

//		if (!isStandalone) {
//			// 不可更改顺序否则不能兼容4.x
//			assetPaths[0] = app;
//			assetPaths[1] = plugin;
//			if ("vivo".equalsIgnoreCase(Build.BRAND) || "oppo".equalsIgnoreCase(Build.BRAND)
//					|| "Coolpad".equalsIgnoreCase(Build.BRAND)) {
//				// 但是！！！如是OPPO或者vivo4.x系统的话 ，要吧这个顺序反过来，否则在混合模式下会找不到资源
//				assetPaths[0] = plugin;
//				assetPaths[1] = app;
//			}
//			LogUtil.d("create Plugin Resource from: ", assetPaths[0], assetPaths[1]);
//		} else {
//			assetPaths[0] = plugin;
//			LogUtil.d("create Plugin Resource from: ", assetPaths[0]);
//		}


		if (!isStandalone) {
			// 不可更改顺序否则不能兼容4.x，如华为P7-Android4.4.2
			assetPaths[0] = plugin;
			if (dependencies != null) {
				//插件间资源依赖，这里需要遍历添加dependencies
				//这里只处理1级依赖，若被依赖的插件又依赖其他插件，这里不做支持
				//插件依赖插件，如果被依赖的插件中包含资源文件，则需要在所有的插件中提供public.xml文件来分组资源id
				for(int i = 0; i < dependencies.length; i++) {
					PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(dependencies[i]);
					if (pd != null) {
						assetPaths[1+ i] = pd.getInstalledPath();
					} else {
						assetPaths[1+ i] = "";
					}
				}
			}
			assetPaths[assetPaths.length -1] = app;
			LogUtil.v("create Plugin Resource from: ", assetPaths[0], assetPaths[1]);
		} else {
			assetPaths[0] = plugin;
			LogUtil.v("create Plugin Resource from: ", assetPaths[0]);
		}

		return assetPaths;

	}

	/**
	 * 创建插件的Context
	 * @return
	 */
	public static Context createPluginContext(PluginDescriptor pluginDescriptor, Context base, Resources pluginRes,
												  DexClassLoader pluginClassLoader) {
		return new PluginContextTheme(pluginDescriptor, base, pluginRes, pluginClassLoader);
	}
}
