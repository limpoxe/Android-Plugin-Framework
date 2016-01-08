package com.plugin.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.core.manager.PluginCallbackImpl;
import com.plugin.core.manager.PluginManagerImpl;
import com.plugin.core.manager.PluginCallback;
import com.plugin.core.manager.PluginManager;
import com.plugin.util.LogUtil;
import com.plugin.util.ManifestParser;
import com.plugin.util.FileUtil;
import com.plugin.util.PackageVerifyer;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

public class PluginLoader {

	private static final boolean NEED_VERIFY_CERT = true;
	private static final int SUCCESS = 0;
	private static final int SRC_FILE_NOT_FOUND = 1;
	private static final int COPY_FILE_FAIL = 2;
	private static final int SIGNATURES_INVALIDATE = 3;
	private static final int VERIFY_SIGNATURES_FAIL = 4;
	private static final int PARSE_MANIFEST_FAIL = 5;
	private static final int FAIL_BECAUSE_HAS_LOADED = 6;
	private static final int INSTALL_FAIL = 7;

	private static Application sApplication;

	private static boolean isLoaderInited = false;

	private static PluginManager pluginManager;

	private static PluginCallback changeListener;

	private PluginLoader() {
	}

	public static synchronized void initLoader(Application app) {
		initLoader(app, new PluginManagerImpl());
	}

	/**
	 * 初始化loader, 只可调用一次
	 * 
	 * @param app
	 */
	public static synchronized void initLoader(Application app, PluginManager manager) {

		if (!isLoaderInited) {
			LogUtil.d("插件框架初始化中...");

			isLoaderInited = true;
			sApplication = app;

			PluginInjector.injectBaseContext(sApplication);

			Object activityThread = PluginInjector.getActivityThread();
			PluginInjector.injectInstrumentation(activityThread);
			PluginInjector.injectHandlerCallback(activityThread);

			pluginManager = manager;
			changeListener = new PluginCallbackImpl();

			pluginManager.loadInstalledPlugins();

			Iterator<PluginDescriptor> itr = getPlugins().iterator();
			while (itr.hasNext()) {
				PluginDescriptor plugin = itr.next();
				LocalServiceManager.registerService(plugin);
			}

			changeListener.onPluginLoaderInited();

			if (Build.VERSION.SDK_INT >= 14) {

				sApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
					@Override
					public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
					}

					@Override
					public void onActivityStarted(Activity activity) {
					}

					@Override
					public void onActivityResumed(Activity activity) {
					}

					@Override
					public void onActivityPaused(Activity activity) {
					}

					@Override
					public void onActivityStopped(Activity activity) {
					}

					@Override
					public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
					}

					@Override
					public void onActivityDestroyed(Activity activity) {
						PluginStubBinding.unBindLaunchModeStubActivity(activity.getClass().getName(), activity.getIntent());
					}
				});
			}
			LogUtil.d("插件框架初始化完成");
		}
	}

	/**
	 * 安装一个插件
	 * 
	 * @param srcPluginFile
	 * @return
	 */
	public static synchronized int installPlugin(String srcPluginFile) {
		LogUtil.d("开始安装插件", srcPluginFile);
		if (TextUtils.isEmpty(srcPluginFile) || !new File(srcPluginFile).exists()) {
			return SRC_FILE_NOT_FOUND;
		}

		//第0步，先将apk复制到宿主程序私有目录，防止在安装过程中文件被篡改
		if (!srcPluginFile.startsWith(sApplication.getCacheDir().getAbsolutePath())) {
			String tempFilePath = sApplication.getCacheDir().getAbsolutePath()
					+ File.separator + System.currentTimeMillis() + ".apk";
			if (FileUtil.copyFile(srcPluginFile, tempFilePath)) {
				srcPluginFile = tempFilePath;
			} else {
				LogUtil.e("复制插件文件失败", srcPluginFile, tempFilePath);
				return COPY_FILE_FAIL;
			}
		}

		// 第1步，验证插件APK签名，如果被篡改过，将获取不到证书
		//sApplication.getPackageManager().getPackageArchiveInfo(srcPluginFile, PackageManager.GET_SIGNATURES);
		Signature[] pluginSignatures = PackageVerifyer.collectCertificates(srcPluginFile, false);
		boolean isDebugable = (0 != (sApplication.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		if (pluginSignatures == null) {
			LogUtil.e("插件签名验证失败", srcPluginFile);
			new File(srcPluginFile).delete();
			return SIGNATURES_INVALIDATE;
		} else if (NEED_VERIFY_CERT && !isDebugable) {
			//可选步骤，验证插件APK证书是否和宿主程序证书相同。
			//证书中存放的是公钥和算法信息，而公钥和私钥是1对1的
			//公钥相同意味着是同一个作者发布的程序
			Signature[] mainSignatures = null;
			try {
				PackageInfo pkgInfo = sApplication.getPackageManager().getPackageInfo(sApplication.getPackageName(), PackageManager.GET_SIGNATURES);
				mainSignatures = pkgInfo.signatures;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			if (!PackageVerifyer.isSignaturesSame(mainSignatures, pluginSignatures)) {
				LogUtil.e("插件证书和宿主证书不一致", srcPluginFile);
				new File(srcPluginFile).delete();
				return VERIFY_SIGNATURES_FAIL;
			}
		}

		// 第2步，解析Manifest，获得插件详情
		PluginDescriptor pluginDescriptor = ManifestParser.parseManifest(srcPluginFile);
		if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getPackageName())) {
			LogUtil.e("解析插件Manifest文件失败", srcPluginFile);
			new File(srcPluginFile).delete();
			return PARSE_MANIFEST_FAIL;
		}

		PackageInfo packageInfo = sApplication.getPackageManager().getPackageArchiveInfo(srcPluginFile, PackageManager.GET_GIDS);
		if (packageInfo != null) {
			pluginDescriptor.setApplicationTheme(packageInfo.applicationInfo.theme);
			pluginDescriptor.setApplicationIcon(packageInfo.applicationInfo.icon);
			pluginDescriptor.setApplicationLogo(packageInfo.applicationInfo.logo);
		}

		// 第3步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
		if (oldPluginDescriptor != null) {
			LogUtil.e("已安装过，安装路径为", oldPluginDescriptor.getInstalledPath());

			//检查插件是否已经加载
			if (oldPluginDescriptor.getPluginContext() != null) {
				LogUtil.e("插件已经加载, 现在不可执行安装操作");
				return FAIL_BECAUSE_HAS_LOADED;
			} else {
				LogUtil.e("先删除已安装的版本");
				remove(oldPluginDescriptor.getPackageName());
			}
		}

		// 第4步骤，复制插件到插件目录
		String destPluginFile = pluginManager.genInstallPath(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
		boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destPluginFile);

		if (!isCopySuccess) {

			LogUtil.d("复制插件到安装目录失败", srcPluginFile);
			//删掉临时文件
			new File(srcPluginFile).delete();
			return COPY_FILE_FAIL;
		} else {

			//第5步，先解压so到临时目录，再从临时目录复制到插件so目录。 在构造插件Dexclassloader的时候，会使用这个so目录作为参数
			File tempDir = new File(new File(destPluginFile).getParentFile(), "temp");
			Set<String> soList = FileUtil.unZipSo(srcPluginFile, tempDir);
			if (soList != null) {
				for (String soName : soList) {
					FileUtil.copySo(tempDir, soName, new File(destPluginFile).getParent());
				}
				//删掉临时文件
				FileUtil.deleteAll(tempDir);
			}

			// 第6步 添加到已安装插件列表
			pluginDescriptor.setInstalledPath(destPluginFile);
			boolean isInstallSuccess = pluginManager.addOrReplace(pluginDescriptor);
			//删掉临时文件
			new File(srcPluginFile).delete();

			if (!isInstallSuccess) {
				LogUtil.d("安装插件失败", srcPluginFile);
				return INSTALL_FAIL;
			} else {
				//通过创建classloader来触发dexopt，但不加载
				LogUtil.d("正在进行DEXOPT...", pluginDescriptor.getInstalledPath());
				PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(), pluginDescriptor.isStandalone(), null);
				LogUtil.d("DEXOPT完毕");

				LocalServiceManager.registerService(pluginDescriptor);

				changeListener.onPluginInstalled(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
				LogUtil.d("安装插件成功", destPluginFile);
				return SUCCESS;
			}
		}
	}

	/**
	 * 通过插件Id唤起插件
	 * @param pluginId
	 * @return
	 */
	public static PluginDescriptor initPluginByPluginId(String pluginId) {
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
			ensurePluginInited(pluginDescriptor);
		}
		return pluginDescriptor;
	}

	/**
	 * 根据插件中的classId加载一个插件中的class
	 * 
	 * @param clazzId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class loadPluginFragmentClassById(String clazzId) {

		PluginDescriptor pluginDescriptor = getPluginDescriptorByFragmenetId(clazzId);

		if (pluginDescriptor != null) {

			ensurePluginInited(pluginDescriptor);

			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();

			String clazzName = pluginDescriptor.getPluginClassNameById(clazzId);
			if (clazzName != null) {
				try {
					Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
					LogUtil.d("loadPluginClass for clazzId", clazzId, "clazzName", clazzName, "success");
					return pluginClazz;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		LogUtil.e("loadPluginClass for clazzId", clazzId, "fail");

		return null;

	}

	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassByName(String clazzName) {

		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazzName);

		if (pluginDescriptor != null) {

			ensurePluginInited(pluginDescriptor);

			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();

			try {
				Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
				LogUtil.d("loadPluginClass Success for clazzName ", clazzName);
				return pluginClazz;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (java.lang.IllegalAccessError illegalAccessError) {
				illegalAccessError.printStackTrace();
				throw new IllegalAccessError("出现这个异常最大的可能是插件dex和" +
						"宿主dex包含了相同的class导致冲突, " +
						"请检查插件的编译脚本，确保排除了所有公共依赖库的jar");
			}

		}

		LogUtil.e("loadPluginClass Fail for clazzName ", clazzName);

		return null;

	}

	/**
	 * 获取当前class所在插件的Context
	 * 每个插件只有1个DefaultContext,
	 * 是当前插件中所有class公用的Context
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getDefaultPluginContext(@SuppressWarnings("rawtypes") Class clazz) {

		Context pluginContext = null;
		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazz.getName());

		if (pluginDescriptor != null) {
			pluginContext = pluginDescriptor.getPluginContext();
		} else {
			LogUtil.e("PluginDescriptor Not Found for ", clazz.getName());
		}

		if (pluginContext == null) {
			LogUtil.e("Context Not Found for ", clazz.getName());
		}

		return pluginContext;
	}

	/**
	 * 根据当前插件的默认Context, 为当前插件的组件创建一个单独的context
	 *
	 * @param pluginContext
	 * @param base  由系统创建的Context。 其实际类型应该是ContextImpl
	 * @return
	 */
	/*package*/ static Context getNewPluginComponentContext(Context pluginContext, Context base) {
		Context newContext = null;
		if (pluginContext != null) {
			newContext = PluginCreator.createPluginContext(((PluginContextTheme) pluginContext).getPluginDescriptor(),
					base, pluginContext.getResources(),
					(DexClassLoader) pluginContext.getClassLoader());
			newContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		}
		return newContext;
	}

	public static Context getNewPluginApplicationContext(Class clazz) {
		Context defaultContext = getDefaultPluginContext(clazz);
		Context newContext = null;
		if (defaultContext != null) {
			newContext = PluginCreator.createPluginContext(((PluginContextTheme) defaultContext).getPluginDescriptor(),
					sApplication, defaultContext.getResources(),
					(DexClassLoader) defaultContext.getClassLoader());
			newContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		}
		return newContext;
	}

	/**
	 * 构造插件信息
	 * 
	 * @param
	 */
	static void ensurePluginInited(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				LogUtil.d("正在初始化插件Resources, DexClassLoader, Context, Application ");

				LogUtil.d("是否为独立插件", pluginDescriptor.isStandalone());

				Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor.getInstalledPath(),
						pluginDescriptor.isStandalone(), pluginDescriptor.getDependencies());

				pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(),
						pluginDescriptor.isStandalone(), pluginDescriptor.getDependencies());
				Context pluginContext = PluginCreator
						.createPluginContext(pluginDescriptor, sApplication, pluginRes, pluginClassLoader);

				pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
				pluginDescriptor.setPluginContext(pluginContext);
				pluginDescriptor.setPluginClassLoader(pluginClassLoader);

				//使用了openAtlasExtention之后就不需要Public.xml文件了
				//checkPluginPublicXml(pluginDescriptor, pluginRes);

				callPluginApplicationOnCreate(pluginDescriptor);

				LogUtil.d("初始化插件" + pluginDescriptor.getPackageName() + "完成");
			}
		}
	}

	private static void callPluginApplicationOnCreate(PluginDescriptor pluginDescriptor) {

		Application application = null;

		if (pluginDescriptor.getPluginApplication() == null && pluginDescriptor.getPluginClassLoader() != null) {
			try {
				LogUtil.d("创建插件Application", pluginDescriptor.getApplicationName());
				application = Instrumentation.newApplication(pluginDescriptor.getPluginClassLoader().loadClass(pluginDescriptor.getApplicationName()) , pluginDescriptor.getPluginContext());
				pluginDescriptor.setPluginApplication(application);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 	catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		//安装ContentProvider
		PluginInjector.installContentProviders(sApplication, pluginDescriptor.getProviderInfos().values());

		//执行onCreate
		if (application != null) {
			application.onCreate();
		}

		changeListener.onPluginStarted(pluginDescriptor.getPackageName());
	}

	public static boolean isInstalled(String pluginId, String pluginVersion) {
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
			LogUtil.d(pluginId, pluginDescriptor.getVersion(), pluginVersion);
			return pluginDescriptor.getVersion().equals(pluginVersion);
		}
		return false;
	}

	public static Application getApplicatoin() {
		return sApplication;
	}

	/**
	 * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
	 */
	public static synchronized void removeAll() {
		boolean isSuccess = pluginManager.removeAll();
		if (isSuccess) {
			changeListener.onPluginRemoveAll();
		}
	}

	public static synchronized void remove(String pluginId) {
		boolean isSuccess = pluginManager.remove(pluginId);
		if (isSuccess) {
			changeListener.onPluginRemoved(pluginId);
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection<PluginDescriptor> getPlugins() {
		return pluginManager.getPlugins();
	}

	/**
	 * for Fragment
	 * 
	 * @param clazzId
	 * @return
	 */
	public static PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId) {
		return pluginManager.getPluginDescriptorByFragmenetId(clazzId);
	}

	public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
		return pluginManager.getPluginDescriptorByPluginId(pluginId);
	}

	public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		return pluginManager.getPluginDescriptorByClassName(clazzName);
	}

	public static synchronized void enablePlugin(String pluginId, boolean enable) {
		pluginManager.enablePlugin(pluginId, enable);
	}

	/**
	 */
	public static ArrayList<String> matchPlugin(Intent intent, int type) {
		ArrayList<String> result = null;

		Iterator<PluginDescriptor> itr = getPlugins().iterator();

		while (itr.hasNext()) {
			PluginDescriptor plugin = itr.next();
			String clazzName = null;
			// 如果是通过组件进行匹配的
			if (intent.getComponent() != null) {
				if (plugin.containsName(intent.getComponent().getClassName())) {
					clazzName = intent.getComponent().getClassName();
					result = new ArrayList<String>(1);
					result.add(clazzName);
					return result;//暂时不考虑不同的插件中配置了相同名称的组件的问题,先到先得
				}
			} else {
				// 如果是通过IntentFilter进行匹配的
				if (type == PluginDescriptor.ACTIVITY) {

					ArrayList<String> list  = findClassNameByIntent(intent, plugin.getActivitys());

					if (list != null && list.size() >0) {
						result = new ArrayList<String>(1);
						result.add(list.get(0));
						return result;//暂时不考虑多个Activity配置了相同的Intent的问题,先到先得
					}

				} else if (type == PluginDescriptor.SERVICE) {

					ArrayList<String> list  = findClassNameByIntent(intent, plugin.getServices());

					if (list != null && list.size() >0) {
						result = new ArrayList<String>(1);
						result.add(list.get(0));
						return result;//service本身不支持多匹配
					}

				} else if (type == PluginDescriptor.BROADCAST) {

					ArrayList<String> list  = findClassNameByIntent(intent, plugin.getReceivers());
					if (list != null) {
						if (result == null) {
							result = new ArrayList<String>();
						}
						result.addAll(list);//暂时不考虑去重的问题
					}
				}
			}
		}
		return result;
	}

	private static ArrayList<String> findClassNameByIntent(Intent intent, HashMap<String, ArrayList<PluginIntentFilter>> intentFilter) {
		if (intentFilter != null) {
			ArrayList<String> targetClassNameList = null;

			Iterator<Entry<String, ArrayList<PluginIntentFilter>>> entry = intentFilter.entrySet().iterator();
			while (entry.hasNext()) {
				Entry<String, ArrayList<PluginIntentFilter>> item = entry.next();
				Iterator<PluginIntentFilter> values = item.getValue().iterator();
				while (values.hasNext()) {
					PluginIntentFilter filter = values.next();
					int result = filter.match(intent.getAction(), intent.getType(), intent.getScheme(),
							intent.getData(), intent.getCategories());

					if (result != PluginIntentFilter.NO_MATCH_ACTION
							&& result != PluginIntentFilter.NO_MATCH_CATEGORY
							&& result != PluginIntentFilter.NO_MATCH_DATA
							&& result != PluginIntentFilter.NO_MATCH_TYPE) {
						if (targetClassNameList == null) {
							targetClassNameList = new ArrayList<String>();
						}
						targetClassNameList.add(item.getKey());
						break;
					}
				}
			}
			return targetClassNameList;
		}
		return null;
	}

}
