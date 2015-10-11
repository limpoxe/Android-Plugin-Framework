package com.plugin.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.text.TextUtils;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
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
	private static final int INSTALL_FAIL = 6;

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
			changeListener.onPluginLoaderInited();

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
				LogUtil.e("复制插件文件失败失败", srcPluginFile, tempFilePath);
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
		pluginDescriptor.setApplicationTheme(packageInfo.applicationInfo.theme);
		pluginDescriptor.setApplicationIcon(packageInfo.applicationInfo.icon);
		pluginDescriptor.setApplicationLogo(packageInfo.applicationInfo.logo);

		// 第3步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
		if (oldPluginDescriptor != null) {
			LogUtil.e("已安装过，先删除旧版本", srcPluginFile);
			remove(pluginDescriptor.getPackageName());
		}

		// 第4步骤，复制插件到插件目录
		String destPluginFile = pluginManager.genInstallPath(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
		boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destPluginFile);

		if (!isCopySuccess) {

			LogUtil.d("复制插件到安装目录失败", srcPluginFile);
			new File(srcPluginFile).delete();
			return COPY_FILE_FAIL;
		} else {

			//第5步，复制插件so到插件so目录, 在构造插件Dexclassloader的时候，会使用这个so目录作为参数
			File tempDir = new File(new File(destPluginFile).getParentFile(), "temp");
			Set<String> soList = FileUtil.unZipSo(srcPluginFile, tempDir);
			if (soList != null) {
				for (String soName : soList) {
					FileUtil.copySo(tempDir, soName, new File(destPluginFile).getParent() + File.separator + "lib");
				}
				FileUtil.deleteAll(tempDir);
			}

			// 第6步 添加到已安装插件列表
			pluginDescriptor.setInstalledPath(destPluginFile);
			boolean isInstallSuccess = pluginManager.addOrReplace(pluginDescriptor);

			if (!isInstallSuccess) {
				new File(srcPluginFile).delete();
				LogUtil.d("安装插件失败", srcPluginFile);
				return INSTALL_FAIL;
			} else {
				changeListener.onPluginInstalled(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
				LogUtil.d("安装插件成功", srcPluginFile);
				return SUCCESS;
			}
		}
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
	 * 根据当前class所在插件的默认Context, 为当前插件Class创建一个单独的context
	 *
	 * 原因在插件Activity中，每个Activity都应当建立独立的Context，
	 *
	 * 而不是都使用同一个defaultContext，避免不同界面的主题和样式互相影响
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getNewPluginContext(@SuppressWarnings("rawtypes") Class clazz) {
		Context pluginContext = getDefaultPluginContext(clazz);
		if (pluginContext != null) {
			pluginContext = PluginCreator.createPluginApplicationContext(((PluginContextTheme)pluginContext).getPluginDescriptor(),
					sApplication, pluginContext.getResources(),
					(DexClassLoader) pluginContext.getClassLoader());
			pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		}
		return pluginContext;
	}

	/**
	 * 构造插件信息
	 * 
	 * @param
	 */
	private static void ensurePluginInited(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				LogUtil.d("正在初始化插件Resources, DexClassLoader, Context, Application ");

				LogUtil.d("是否为独立插件", pluginDescriptor.isStandalone());

				Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor.getInstalledPath(),
						pluginDescriptor.isStandalone());

				pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(),
						pluginDescriptor.isStandalone());
				Context pluginContext = PluginCreator
						.createPluginApplicationContext(pluginDescriptor, sApplication, pluginRes, pluginClassLoader);

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

		if (pluginDescriptor.getApplicationName() != null && pluginDescriptor.getPluginApplication() == null
				&& pluginDescriptor.getPluginClassLoader() != null) {
			try {
				LogUtil.d("创建插件Application", pluginDescriptor.getApplicationName());
				application = Instrumentation.newApplication(pluginDescriptor.getPluginClassLoader().loadClass(pluginDescriptor.getApplicationName()) , sApplication);
				pluginDescriptor.setPluginApplication(application);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 	catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		PluginInjector.installContentProviders(sApplication, pluginDescriptor.getProviderInfos().values());

		if (application != null) {
			application.onCreate();
		}

		changeListener.onPluginStarted(pluginDescriptor.getPackageName());
	}

	/**
	 * for eclipse & ant with public.xml
	 *
	 * unused
	 * @param pluginDescriptor
	 * @param res
	 * @return
	 */
	private static boolean checkPluginPublicXml(PluginDescriptor pluginDescriptor, Resources res) {

		// "plugin_layout_1"资源id时由public.xml配置的
		// 如果没有检测到这个资源，说明编译时没有引入public.xml,
		// 这里直接抛个异常出去。
		// 不同的系统版本获取id的方式不同，
		// 三星4.x等系统适用
		int publicStub = res.getIdentifier("plugin_layout_1", "layout", pluginDescriptor.getPackageName());
		if (publicStub == 0) {
			// 小米5.x等系统适用
			publicStub = res.getIdentifier("plugin_layout_1", "layout", sApplication.getPackageName());
		}
		if (publicStub == 0) {
			try {
				// 如果以上两种方式都检测失败，最后尝试通过反射检测
				Class layoutClass = ((ClassLoader) pluginDescriptor.getPluginClassLoader()).loadClass(pluginDescriptor
						.getPackageName() + ".R$layout");
				Integer layouId = (Integer) RefInvoker.getFieldObject(null, layoutClass, "plugin_layout_1");
				if (layouId != null) {
					publicStub = layouId;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (publicStub == 0) {
			throw new IllegalStateException("\n插件工程没有使用public.xml给资源id分组！！！\n" + "插件工程没有使用public.xml给资源id分组！！！\n"
					+ "插件工程没有使用public.xml给资源id分组！！！\n" + "重要的事情讲三遍！！！");
		}
		return true;
	}

	public static boolean isInstalled(String pluginId, String pluginVersion) {
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
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
	 * //If getComponent returns an explicit class, that is returned without any
	 * further consideration. //If getAction is non-NULL, the activity must
	 * handle this action. //If resolveType returns non-NULL, the activity must
	 * handle this type. //If addCategory has added any categories, the activity
	 * must handle ALL of the categories specified. //If getPackage is non-NULL,
	 * only activity components in that application package will be considered.
	 * 
	 * @param intent
	 * @return
	 */
	public static String matchPlugin(Intent intent) {

		Iterator<PluginDescriptor> itr = getPlugins().iterator();

		while (itr.hasNext()) {
			PluginDescriptor plugin = itr.next();
			// 如果是通过组件进行匹配的
			if (intent.getComponent() != null) {
				if (plugin.containsName(intent.getComponent().getClassName())) {
					return intent.getComponent().getClassName();
				}
			} else {
				// 如果是通过IntentFilter进行匹配的
				String clazzName = findClassNameByIntent(intent, plugin.getActivitys());

				if (clazzName == null) {
					clazzName = findClassNameByIntent(intent, plugin.getServices());
				}

				if (clazzName == null) {
					clazzName = findClassNameByIntent(intent, plugin.getReceivers());
				}

				if (clazzName != null) {
					return clazzName;
				}
			}

		}
		return null;
	}

	/**
	 * 获取目标类型，activity or service or broadcast
	 * @param intent
	 * @return
	 */
	public static int getTargetType(Intent intent) {

		Iterator<PluginDescriptor> itr = getPlugins().iterator();

		while (itr.hasNext()) {
			PluginDescriptor plugin = itr.next();
			// 如果是通过组件进行匹配的
			if (intent.getComponent() != null) {
				if (plugin.containsName(intent.getComponent().getClassName())) {
					return plugin.getType(intent.getComponent().getClassName());
				}
			} else {
				String clazzName = findClassNameByIntent(intent, plugin.getActivitys());

				if (clazzName == null) {
					clazzName = findClassNameByIntent(intent, plugin.getServices());
				}

				if (clazzName == null) {
					clazzName = findClassNameByIntent(intent, plugin.getReceivers());
				}

				if (clazzName != null) {
					return plugin.getType(clazzName);
				}
			}
		}
		return PluginDescriptor.UNKOWN;
	}

	private static String findClassNameByIntent(Intent intent, HashMap<String, ArrayList<PluginIntentFilter>> intentFilter) {
		if (intentFilter != null) {

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
						return item.getKey();
					}
				}
			}
		}
		return null;
	}

}
