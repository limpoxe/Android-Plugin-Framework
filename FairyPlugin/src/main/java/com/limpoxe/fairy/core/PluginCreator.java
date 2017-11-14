package com.limpoxe.fairy.core;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.android.HackAssetManager;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;

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
                    dependences,//通常情况独立插件无子依赖, 此处参数size一般是0，但实际也可以依赖其他基础独立插件包，
                                // 也即独立插件之间也可以建立依赖关系，前提和非独立插件一样，被依赖的插件不可以包含资源
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
				HackAssetManager hackAssetManager = new HackAssetManager(assetMgr);
				hackAssetManager.addAssetPaths(assetPaths);

				// Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
				// in L, so we do it unconditionally.
				hackAssetManager.ensureStringBlocks();

				Resources pluginRes = new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(),
						mainRes.getConfiguration(), pluginDescriptor);

				return pluginRes;
			} catch (Exception e) {
                LogUtil.printException("创建插件res失败" + absolutePluginApkPath, e);
            }
		} else {
			LogUtil.e("插件文件不存在", absolutePluginApkPath);
            LogUtil.e("插件文件可能已损坏，正在卸载此插件...");
            PluginManagerHelper.remove(pluginDescriptor.getPackageName());
            LogUtil.e("卸载完成");
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

		// 不可更改顺序否则不能兼容4.x，如华为P7-Android4.4.2
		assetPaths[0] = plugin;
		LogUtil.v("create Plugin Resource from: ", plugin);

        if (!isStandalone) {
			if (dependencies != null) {
				//插件间资源依赖，这里需要遍历添加dependencies
				//这里只处理1级依赖，若被依赖的插件又依赖其他插件，这里不做支持
				//插件依赖插件，如果被依赖的插件中包含资源文件，则需要在所有的插件中提供public.xml文件来分组资源id
				for(int i = 0; i < dependencies.length; i++) {
					PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(dependencies[i]);
					if (pd != null) {
						assetPaths[1+ i] = pd.getInstalledPath();
						LogUtil.v("create Plugin Resource from: ", assetPaths[1+ i]);
					} else {
						assetPaths[1+ i] = "";
					}
				}
			}
			assetPaths[assetPaths.length -1] = app;
			LogUtil.v("create Plugin Resource from: ", app);
		}

		return assetPaths;

	}


	/**
	private static String[] buildAssetPath(boolean isStandalone, String host, String plugin, String[] dependencies) {


	 	//暂不支持资源多级依赖, 因为目前资源id采用type分组，
	 	//多级依赖编译时在type不变的情况下需要同时配合分段来完成，过于复杂。
	 	//除非采用aapt-packageId分组方式
	 	//
		dependencies = null;

		ArrayList<String> paths = new ArrayList<String>();
		AssetManager hostAssetsManager = PluginLoader.getHostApplication().getAssets();
		Integer pathCount = (Integer)RefInvoker.invokeMethod(hostAssetsManager,
				AssetManager.class, "getStringBlockCount", null, null);
		if (pathCount != null) {
			for(int cookie = 0; cookie < pathCount; cookie++) {
				String assetsPath = (String)RefInvoker.invokeMethod(hostAssetsManager,
						AssetManager.class, "getCookieName",
						new Class[]{int.class}, new Object[]{cookie + 1});
				if(!TextUtils.isEmpty(assetsPath)) {
					//不可更改顺序否则不能兼容4.x，如华为P7-Android4.4.2
					if (!assetsPath.equals(host)) {
						paths.add(assetsPath);
						LogUtil.d("create Plugin Resource from: ", assetsPath);
					} else {
						paths.add(plugin);
						LogUtil.d("create Plugin Resource from: ", plugin);
						if (!isStandalone) {
							if (dependencies != null) {
								//插件间资源依赖，需要遍历添加dependencies
								//这里只处理1级依赖，若被依赖的插件又依赖其他插件，这里不做支持
								//插件依赖插件，如果被依赖的插件中包含资源文件，则需要在所有的插件中提供public.xml文件来分组和分段资源id
								for(int j = 0; j < dependencies.length; j++) {
									PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(dependencies[j]);
									if (pd != null) {
										paths.add(pd.getInstalledPath());
										LogUtil.d("create Plugin Resource from: ", pd.getInstalledPath());
									}
								}
							}
							paths.add(assetsPath);
							LogUtil.d("create Plugin Resource from: ", assetsPath);

						}
					}
				}
			}
		}
		return paths.toArray(new String[paths.size()]);
	}*/

	/**
	 * 创建插件的Context
	 * @return
	 */
	public static Context createPluginContext(PluginDescriptor pluginDescriptor, Context base, Resources pluginRes,
												  DexClassLoader pluginClassLoader) {
		return new PluginContextTheme(pluginDescriptor, base, pluginRes, pluginClassLoader);
	}

    /*package*/ static Context getNewPluginApplicationContext(String pluginId) {
        PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
        if (pluginDescriptor == null) {
            return null;
        }
        //插件可能尚未初始化，确保使用前已经初始化
        LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);

        if (plugin != null) {
            PluginContextTheme newContext = (PluginContextTheme)PluginCreator.createPluginContext(
                    ((PluginContextTheme) plugin.pluginContext).getPluginDescriptor(),
                    FairyGlobal.getHostApplication().getBaseContext(), plugin.pluginResource, plugin.pluginClassLoader);

            newContext.setPluginApplication(plugin.pluginApplication);

            newContext.setTheme(pluginDescriptor.getApplicationTheme());

            return newContext;
        }

        return null;
    }

    /**
     * 根据当前插件的默认Context, 为当前插件的组件创建一个单独的context
     *
     * @param pluginContext
     * @param base  由系统创建的Context。 其实际类型应该是ContextImpl
     * @return
     */
	/*package*/ static Context createNewPluginComponentContext(Context pluginContext, Context base, int theme) {
        PluginContextTheme newContext = null;
        if (pluginContext != null) {
            newContext = (PluginContextTheme)PluginCreator.createPluginContext(((PluginContextTheme) pluginContext).getPluginDescriptor(),
                    base, pluginContext.getResources(),
                    (DexClassLoader) pluginContext.getClassLoader());

            newContext.setPluginApplication((Application) ((PluginContextTheme) pluginContext).getApplicationContext());

            newContext.setTheme(FairyGlobal.getHostApplication().getApplicationContext().getApplicationInfo().theme);
        }
        return newContext;
    }
}
