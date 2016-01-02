package com.plugin.core;

import java.io.File;
import java.lang.reflect.Method;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
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
														 String[] dependences) {
		if (!isStandalone) {//非独立插件
			return new PluginClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(),
					new File(absolutePluginApkPath).getParent() + File.separator + "lib",
					PluginLoader.class.getClassLoader(), dependences);//宿主classloader
		} else {//独立插件
			return new PluginClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(),
					new File(absolutePluginApkPath).getParent() + File.separator + "lib",
					PluginLoader.class.getClassLoader().getParent(), null);//系统classloader
		}

	}

	/**
	 * 根据插件apk文件，创建插件资源文件，同时绑定宿主程序的资源，这样就可以在插件中使用宿主程序的资源。
	 * 
	 * @param application
	 *            宿主程序的Application
	 * @param absolutePluginApkPath
	 *            插件apk文件路径
	 * @return
	 */
	public static Resources createPluginResource(Application application, String absolutePluginApkPath,
			boolean isStandalone, String[] dependencies) {
		try {

			// 插件运行时可能会通过getActivityInfo等
			// 会拿到到PluginStubActivity的ActivityInfo以及ApplicationInfo
			// 这两个info里面有部分资源id是在宿主程序的Manifest中配置的，比如logo和icon
			// 如果在独立插件中尝试通过Context获取上述这些资源会导致异常
			// 所以为了解决这个问题，利用宿主程序的资源id已经通过public.xml分过组了，这里强制对独立插件也进行资源合并操作
			isStandalone = false;

			String[] assetPaths = buildAssetPath(isStandalone, application.getApplicationInfo().sourceDir,
					absolutePluginApkPath, dependencies);
			AssetManager assetMgr = AssetManager.class.newInstance();
			RefInvoker.invokeMethod(assetMgr, AssetManager.class.getName(), "addAssetPaths",
					new Class[] { String[].class }, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(),
					mainRes.getConfiguration());

			return pluginRes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String[] buildAssetPath(boolean isStandalone, String app, String plugin, String[] dependencies) {
		String[] assetPaths = new String[isStandalone ? 1 : 2];

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


		//若需支持插件间资源依赖，这里需要遍历添加dependencies

		if (!isStandalone) {
			// 不可更改顺序否则不能兼容4.x，如华为P7-Android4.4.2
			assetPaths[0] = plugin;
			assetPaths[1] = app;
			LogUtil.d("create Plugin Resource from: ", assetPaths[0], assetPaths[1]);
		} else {
			assetPaths[0] = plugin;
			LogUtil.d("create Plugin Resource from: ", assetPaths[0]);
		}

		return assetPaths;

	}

	/**
	 * 创建插件的Context
	 * @return
	 */
	static Context createPluginContext(PluginDescriptor pluginDescriptor, Context base, Resources pluginRes,
												  DexClassLoader pluginClassLoader) {
		return new PluginContextTheme(pluginDescriptor, base, pluginRes, pluginClassLoader);
	}
}
