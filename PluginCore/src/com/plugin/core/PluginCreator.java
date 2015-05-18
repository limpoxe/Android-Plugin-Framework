package com.plugin.core;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class PluginCreator {

	private static final String LOG_TAG = PluginCreator.class.getSimpleName();

	private PluginCreator() {
	}

	/**
	 * 根据插件apk文件，创建插件dex的classloader
	 * 
	 * @param absolutePluginApkPath
	 *            插件apk文件路径
	 * @return
	 */
	public static DexClassLoader createPluginClassLoader(String absolutePluginApkPath) {
		return new DexClassLoader(absolutePluginApkPath, new File(absolutePluginApkPath).getParent(), null,
				PluginLoader.class.getClassLoader());
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
	public static Resources createPluginResource(Application application, String absolutePluginApkPath) {
		try {
			AssetManager assetMgr = AssetManager.class.newInstance();
			Method addAssetPaths = AssetManager.class.getDeclaredMethod("addAssetPaths", String[].class);

			String[] assetPaths = new String[2];

			//不可更改顺序否则不能兼容4.x
			//TODO 但是！！！如是OPPO或者vivo4.x系统的话 ，要吧这个顺序反过来，否则在混合模式下会找不到资源
			assetPaths[0] = application.getApplicationInfo().sourceDir;
			assetPaths[1] = absolutePluginApkPath;

			addAssetPaths.invoke(assetMgr, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new Resources(assetMgr, mainRes.getDisplayMetrics(), mainRes.getConfiguration());

			Log.e(LOG_TAG, "create Plugin Resource from: " + assetPaths[0] + ", " + assetPaths[1]);

			return pluginRes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*package*/ static Resources createPluginResourceFor5(Application application, String absolutePluginApkPath) {
		try {
			AssetManager assetMgr = AssetManager.class.newInstance();
			Method addAssetPaths = AssetManager.class.getDeclaredMethod("addAssetPaths", String[].class);

			String[] assetPaths = new String[2];

			//不可更改顺序否则不能兼容4.x
			assetPaths[0] = absolutePluginApkPath;
			assetPaths[1] = application.getApplicationInfo().sourceDir;

			addAssetPaths.invoke(assetMgr, new Object[] { assetPaths });

			Resources mainRes = application.getResources();
			Resources pluginRes = new Resources(assetMgr, mainRes.getDisplayMetrics(), mainRes.getConfiguration());

			Log.e(LOG_TAG, "create Plugin Resource from: " + assetPaths[0] + ", " + assetPaths[1]);

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
