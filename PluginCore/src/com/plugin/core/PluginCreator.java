package com.plugin.core;

import java.io.File;
import java.lang.reflect.Method;

import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
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
	public static DexClassLoader createPluginClassLoader(String absolutePluginApkPath, boolean isStandalone) {
		if (!isStandalone) {
			return new DexClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(),
					new File(absolutePluginApkPath).getParent() + File.separator + "lib",
					PluginLoader.class.getClassLoader());
		} else {
			return new DexClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(),
					new File(absolutePluginApkPath).getParent() + File.separator + "lib",
					PluginLoader.class.getClassLoader().getParent());
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
			boolean isStandalone) {
		try {
			// 如果是第三方编译的独立插件的话，插件的资源id默认是0x7f开头。
			// 但是宿主程序的资源id必须使用默认值0x7f（否则在5.x系统上主题会由问题）
			// 插件运行时可能会通过getActivityInfo等
			// 会拿到到PluginStubActivity的ActivityInfo以及ApplicationInfo
			// 这两个info里面有部分资源id是在宿主程序的Manifest中配置的，比如logo和icon
			// 所有如果在独立插件中尝试通过Context获取上述这些资源会导致异常
			String[] assetPaths = buildAssetPath(isStandalone, application.getApplicationInfo().sourceDir,
					absolutePluginApkPath);
			AssetManager assetMgr = AssetManager.class.newInstance();
			RefInvoker.invokeMethod(assetMgr, AssetManager.class.getName(), "addAssetPaths",
					new Class[] { String[].class }, new Object[] { assetPaths });
			// Method addAssetPaths =
			// AssetManager.class.getDeclaredMethod("addAssetPaths",
			// String[].class);
			// addAssetPaths.invoke(assetMgr, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(),
					mainRes.getConfiguration());

			return pluginRes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String[] buildAssetPath(boolean isStandalone, String app, String plugin) {
		String[] assetPaths = new String[isStandalone ? 1 : 2];

		if (!isStandalone) {
			// 不可更改顺序否则不能兼容4.x
			assetPaths[0] = app;
			assetPaths[1] = plugin;
			if ("vivo".equalsIgnoreCase(Build.BRAND) || "oppo".equalsIgnoreCase(Build.BRAND)
					|| "Coolpad".equalsIgnoreCase(Build.BRAND)) {
				// 但是！！！如是OPPO或者vivo4.x系统的话 ，要吧这个顺序反过来，否则在混合模式下会找不到资源
				assetPaths[0] = plugin;
				assetPaths[1] = app;
			}
			LogUtil.d("create Plugin Resource from: ", assetPaths[0], assetPaths[1]);
		} else {
			assetPaths[0] = plugin;
			LogUtil.d("create Plugin Resource from: ", assetPaths[0]);
		}

		return assetPaths;

	}

	/* package */static Resources createPluginResourceFor5(Application application, String absolutePluginApkPath) {
		try {
			AssetManager assetMgr = AssetManager.class.newInstance();
			Method addAssetPaths = AssetManager.class.getDeclaredMethod("addAssetPaths", String[].class);

			String[] assetPaths = new String[2];

			// 不可更改顺序否则不能兼容4.x
			assetPaths[0] = absolutePluginApkPath;
			assetPaths[1] = application.getApplicationInfo().sourceDir;

			addAssetPaths.invoke(assetMgr, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(),
					mainRes.getConfiguration());

			LogUtil.d("create Plugin Resource from: ", assetPaths[0], assetPaths[1]);

			return pluginRes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建插件apk的Context。
	 * 如果插件是运行在普通的Activity中，那么插件中需要使用context的地方，都需要使用此方法返回的Context
	 * 
	 * @param application
	 * @param pluginRes
	 * @param pluginClassLoader
	 * @return
	 */
	static Context createPluginApplicationContext(Application application, Resources pluginRes,
			DexClassLoader pluginClassLoader) {
		return new PluginContextTheme(application, pluginRes, pluginClassLoader);
	}

}
