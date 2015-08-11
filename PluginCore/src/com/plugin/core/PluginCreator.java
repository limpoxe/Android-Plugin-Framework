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
			return new DexClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(), null,
					PluginLoader.class.getClassLoader());
		} else {
			return new DexClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(), null,
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
			// 如果是独立插件的话，本来是可以不合并主程序资源的。
			// 但是由于插件运行时可能会通过getActivityInfo等
			// 会拿到到PluginStubActivity的ActivityInfo以及ApplicationInfo
			// 这两个info里面有部分资源id是在宿主程序的Manifest中配置的，比如logo和icon
			// 尝试通过插件Context获取这些资源会导致异常
			// 所以这里强制合并资源。
			// 强制合并资源，又需要另外一个前提条件，即id不重复。
			// 所以不管是独立插件还是非独立插件，都需要在编译时引入public.xml文件来给资源id分组
			String[] assetPaths = buildAssetPath(false, application.getApplicationInfo().sourceDir,
					absolutePluginApkPath);
			AssetManager assetMgr = AssetManager.class.newInstance();
			RefInvoker.invokeMethod(assetMgr, AssetManager.class.getName(), "addAssetPaths",
					new Class[] { String[].class }, new Object[] { assetPaths });
			// Method addAssetPaths =
			// AssetManager.class.getDeclaredMethod("addAssetPaths",
			// String[].class);
			// addAssetPaths.invoke(assetMgr, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(), mainRes.getConfiguration());

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
			Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(), mainRes.getConfiguration());

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
	public static Context createPluginApplicationContext(Application application, Resources pluginRes,
			DexClassLoader pluginClassLoader) {
		return new PluginContextTheme(application, pluginRes, pluginClassLoader);
	}

	/**
	 * 创建插件apk的Activity context，
	 * 如果用此Context替换宿主程序的Activity的baseContext，那么插件中需要使用context的地方，和非插件开发时完全一致。
	 * 即开发出来的插件代码，和普通的应用程序代码没有区别。 这个方法由于传入了Activity，为了避免泄漏，切记不可长久保持催此方法返回值的引用
	 * 
	 * @param activity
	 * @param pluginContext
	 * @return
	 */
	public static Context createPluginActivityContext(Activity activity, Context pluginContext) {
		return new PluginContextTheme(activity, pluginContext.getResources(), pluginContext.getClassLoader());
	}

}
