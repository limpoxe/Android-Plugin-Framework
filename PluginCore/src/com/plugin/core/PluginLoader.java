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
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
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
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

public class PluginLoader {
	private static Application sApplication;
	private static Object activityThread;
	private static PluginManager pluginManager;
	private static PluginCallback changeListener;

	private static boolean isInited = false;

	private PluginLoader() {
	}

	/**
	 * 初始化loader, 只可调用一次
	 * 
	 * @param app
	 */
	public static synchronized void initLoader(Application app, PluginManager manager) {
		if (!isInited) {

			LogUtil.d("插件框架初始化中...");

			isInited = true;

			sApplication = app;

			initApplicationBaseContext();
			initActivityThread();
			injectInstrumentation();
			injectHandlerCallback();

			pluginManager = manager;
			pluginManager.loadInstalledPlugins();

			changeListener = new PluginCallbackImpl();
			changeListener.onPluginLoaderInited();

			LogUtil.d("插件框架初始化完成");
		}
	}

	public static synchronized void initLoader(Application app) {
		initLoader(app, new PluginManagerImpl());
	}

	public static Application getApplicatoin() {
		return sApplication;
	}

	/**
	 * 替换Application的mBase是为了重载它的几个startactivity、startservice和sendbroadcast方法
	 */
	private static void initApplicationBaseContext() {

		LogUtil.d("替换宿主程序Application baseContext");

		Context base = (Context)RefInvoker.getFieldObject(sApplication, ContextWrapper.class.getName(), "mBase");
		Context newBase = new PluginBaseContextWrapper(base);
		RefInvoker.setFieldObject(sApplication, ContextWrapper.class.getName(), "mBase", newBase);
	}

	private static void initActivityThread() {
		// 从ThreadLocal中取出来的
		LogUtil.d("获取宿主程序ActivityThread对象");
		activityThread = RefInvoker.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread",
				(Class[]) null, (Object[]) null);
	}

	/*package*/ static Object getActivityThread() {
		return activityThread;
	}

	/**
	 * 注入Instrumentation主要是为了支持Activity
	 */
	private static void injectInstrumentation() {
		// 给Instrumentation添加一层代理，用来实现隐藏api的调用
		LogUtil.d("替换宿主程序Intstrumentation");
		Instrumentation originalInstrumentation = (Instrumentation) RefInvoker.getFieldObject(activityThread,
				"android.app.ActivityThread", "mInstrumentation");
		RefInvoker.setFieldObject(activityThread, "android.app.ActivityThread", "mInstrumentation",
				new PluginInstrumentionWrapper(originalInstrumentation));
	}

	private static void injectHandlerCallback() {

		LogUtil.d("向插入宿主程序消息循环插入回调器");

		// getHandler
		Handler handler = (Handler) RefInvoker.invokeMethod(activityThread, "android.app.ActivityThread", "getHandler", (Class[])null, (Object[])null);
		//下面的方法再api16及一下会失败，成员变量名称错误。
		//Handler handler = (Handler) RefInvoker.getStaticFieldObject("android.app.ActivityThread", "sMainThreadHandler");

		// 给handler添加一个callback
		RefInvoker.setFieldObject(handler, Handler.class.getName(), "mCallback", new PluginAppTrace(handler));
	}

	public static boolean isInstalled(String pluginId, String pluginVersion) {
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
			return pluginDescriptor.getVersion().equals(pluginVersion);
		}
		return false;
	}

	/**
	 * 安装一个插件
	 * 
	 * @param srcPluginFile
	 * @return
	 */
	public static synchronized boolean installPlugin(String srcPluginFile) {
		LogUtil.d("Install plugin ", srcPluginFile);

		boolean isInstallSuccess = false;
		// 第一步，读取插件描述文件
		PluginDescriptor pluginDescriptor = ManifestParser.parseManifest(srcPluginFile);
		if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getPackageName())) {
			return isInstallSuccess;
		}

		// 第二步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
		if (oldPluginDescriptor != null) {
			remove(pluginDescriptor.getPackageName());
		}

		// 第三步骤，复制插件到插件目录
		if (pluginDescriptor != null) {

			String destPluginFile = genInstallPath(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destPluginFile);
			if (isCopySuccess) {

				//第四步，复制插件so到插件so目录, 在构造插件Dexclassloader的时候，会使用这个so目录作为参数
				File tempDir = new File(new File(destPluginFile).getParentFile(), "temp");
				Set<String> soList = FileUtil.unZipSo(srcPluginFile, tempDir);
				if (soList != null) {
					for (String soName : soList) {
						FileUtil.copySo(tempDir, soName, new File(destPluginFile).getParent() + File.separator + "lib");
					}
				}

				// 第五步 添加到已安装插件列表
				pluginDescriptor.setInstalledPath(destPluginFile);
				isInstallSuccess = pluginManager.addOrReplace(pluginDescriptor);

				if (isInstallSuccess) {
					changeListener.onPluginInstalled(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
				}
			}
		}

		return isInstallSuccess;
	}

	/**
	 * 根据插件中的classId加载一个插件中的class
	 * 
	 * @param clazzId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassById(String clazzId) {
		LogUtil.d("loadPluginClass for clazzId ", clazzId);

		PluginDescriptor pluginDescriptor = getPluginDescriptorByFragmenetId(clazzId);
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				initPlugin(pluginDescriptor);
				pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			}

			if (pluginClassLoader != null) {
				String clazzName = pluginDescriptor.getPluginClassNameById(clazzId);
				LogUtil.d("loadPluginClass clazzName=", clazzName);
				if (clazzName != null) {
					try {
						Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
						LogUtil.d("loadPluginClass for classId ", clazzId, " Success");
						return pluginClazz;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		LogUtil.d("loadPluginClass for classId ", clazzId, " Fail");
		return null;

	}

	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassByName(String clazzName) {
		LogUtil.d("loadPluginClass for clazzName ", clazzName);

		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazzName);
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				initPlugin(pluginDescriptor);
				pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			}

			if (pluginClassLoader != null) {

				try {
					Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
					LogUtil.d("loadPluginClass Success for clazzName ", clazzName);
					return pluginClazz;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		}

		LogUtil.d("loadPluginClass Fail for clazzName ", clazzName);
		return null;

	}

	/**
	 * 获取当前class所在插件的Context 每个插件只有1个DefaultContext,是当前插件中所有class公用的Context
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getDefaultPluginContext(@SuppressWarnings("rawtypes") Class clazz) {

		// clazz.getClassLoader(); 直接获取classloader的方式，
		// 如果同一个插件安装两次，但是宿主程序进程没有重启，那么得到的classloader可能是前次安装时的loader
		Context pluginContext = null;
		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazz.getName());
		if (pluginDescriptor != null) {
			pluginContext = pluginDescriptor.getPluginContext();
		} else {
			LogUtil.d("PluginDescriptor Not Found for ", clazz.getName());
		}

		if (pluginContext == null) {
			LogUtil.d("Context Not Found for ", clazz.getName());
		}

		return pluginContext;

	}

	/**
	 * 获取当前class所在插件的Context 为当前 插件class创建一个单独的context
	 * 在插件Activity中，每个Activity都应当建立独立的Context，避免主题和样式互相影响
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
	private static void initPlugin(PluginDescriptor pluginDescriptor) {

		LogUtil.d("正在初始化插件Resources, DexClassLoader, Context, Application ");

		LogUtil.d("是否为独立插件", pluginDescriptor.isStandalone());

		Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor.getInstalledPath(),
				pluginDescriptor.isStandalone());

		DexClassLoader pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(),
				pluginDescriptor.isStandalone());
		Context pluginContext = PluginCreator
				.createPluginApplicationContext(pluginDescriptor, sApplication, pluginRes, pluginClassLoader);

		pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		pluginDescriptor.setPluginContext(pluginContext);
		pluginDescriptor.setPluginClassLoader(pluginClassLoader);

		//使用了openAtlasExtention之后就不需要Public.xml文件了
		//checkPluginPublicXml(pluginDescriptor, pluginRes);

		callPluginApplicationOncreate(pluginDescriptor);

		LogUtil.d("初始化插件" + pluginDescriptor.getPackageName() + "完成");
	}

	private static void callPluginApplicationOncreate(PluginDescriptor pluginDescriptor) {

		Application application = null;

		if (pluginDescriptor.getApplicationName() != null && pluginDescriptor.getPluginApplication() == null
				&& pluginDescriptor.getPluginClassLoader() != null) {
			try {
				LogUtil.d("创建插件Application", pluginDescriptor.getApplicationName());
				application = Instrumentation.newApplication(pluginDescriptor.getPluginClassLoader().loadClass(pluginDescriptor.getApplicationName()) , sApplication);
				pluginDescriptor.setPluginApplication(application);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		LogUtil.d("安装插件ContextProvider", pluginDescriptor.getProviderInfos().size());
		PluginContentProviderInstaller.installContentProviders(sApplication, pluginDescriptor.getProviderInfos().values());

		if (application != null) {
			application.onCreate();
		}

		changeListener.onPluginStarted(pluginDescriptor.getPackageName());
	}

	/**
	 * for eclipse with public.xml
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
	public static String isMatchPlugin(Intent intent) {

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


	/**
	 * 插件的安装目录, 插件apk将来会被放在这个目录下面
	 */
	private static String genInstallPath(String pluginId, String pluginVersoin) {
		return sApplication.getDir("plugin_dir", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginId + "/"
				+ pluginVersoin + ".apk";
	}

}
