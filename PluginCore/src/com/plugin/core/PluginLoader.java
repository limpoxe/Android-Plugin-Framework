package com.plugin.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.app.ActivityThread;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.core.manager.PluginManagerImpl;
import com.plugin.core.manager.PluginManager;
import com.plugin.core.systemservice.AndroidAppIActivityManager;
import com.plugin.core.systemservice.AndroidAppINotificationManager;
import com.plugin.core.systemservice.AndroidAppIPackageManager;
import com.plugin.core.systemservice.AndroidViewLayoutInflater;
import com.plugin.core.systemservice.AndroidWebkitWebViewFactoryProvider;
import com.plugin.core.systemservice.AndroidWidgetToast;
import com.plugin.util.LogUtil;
import com.plugin.util.ProcessUtil;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

public class PluginLoader {

	private static Application sApplication;
	private static boolean isLoaderInited = false;

	private static PluginManager pluginManager;

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
			pluginManager = manager;

			AndroidAppIActivityManager.installProxy();
			AndroidAppINotificationManager.installProxy();
			AndroidAppIPackageManager.installProxy(sApplication.getPackageManager());

			if (ProcessUtil.isPluginProcess()) {
				AndroidWidgetToast.installProxy();
				AndroidViewLayoutInflater.installPluginCustomViewConstructorCache();
				//不可在主进程中同步安装，因为此时ActivityThread还没有准备好, 会导致空指针。
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						AndroidWebkitWebViewFactoryProvider.installProxy();
					}
				});
				PluginInjector.injectHandlerCallback();
			}

			PluginInjector.injectInstrumentation();
			PluginInjector.injectBaseContext(sApplication);

			pluginManager.loadInstalledPlugins();

			if (ProcessUtil.isPluginProcess()) {
				Iterator<PluginDescriptor> itr = getPlugins().iterator();
				while (itr.hasNext()) {
					PluginDescriptor plugin = itr.next();
					LocalServiceManager.registerService(plugin);
				}
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
			}
			LogUtil.d("插件框架初始化完成");
		}
	}

	public static Context fixBaseContextForReceiver(Context superApplicationContext) {
		if (superApplicationContext instanceof ContextWrapper) {
			return ((ContextWrapper)superApplicationContext).getBaseContext();
		} else {
			return superApplicationContext;
		}
	}

	public static int installPlugin(String srcFile) {
		return pluginManager.installPlugin(srcFile);
	}

	/**
	 * 通过插件Id唤起插件
	 * @param pluginId
	 * @return
	 */
	public static PluginDescriptor ensurePluginInited(String pluginId) {
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

	public static Context getDefaultPluginContext(String pluginId) {
		Context pluginContext = null;
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);

		if (pluginDescriptor != null) {
			pluginContext = pluginDescriptor.getPluginContext();
		} else {
			LogUtil.e("PluginDescriptor Not Found for ", pluginId);
		}

		if (pluginContext == null) {
			LogUtil.e("Context Not Found for ", pluginId);
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
	/*package*/ static Context getNewPluginComponentContext(Context pluginContext, Context base, int theme) {
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
		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazz.getName());
		return newDefaultAppContext(pluginDescriptor);
	}

	public static Context getNewPluginApplicationContext(String pluginId) {
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
		return newDefaultAppContext(pluginDescriptor);
	}

	private static Context newDefaultAppContext(PluginDescriptor pluginDescriptor) {
		Context newContext = null;
		if (pluginDescriptor != null && pluginDescriptor.getPluginContext() != null) {
			Context originContext = pluginDescriptor.getPluginContext();
			newContext = PluginCreator.createPluginContext(((PluginContextTheme) originContext).getPluginDescriptor(),
					sApplication, originContext.getResources(),
					(DexClassLoader) originContext.getClassLoader());
			newContext.setTheme(pluginDescriptor.getApplicationTheme());
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
				LogUtil.e("正在初始化插件" + pluginDescriptor.getPackageName() + "Resources, DexClassLoader, Context, Application");

				LogUtil.d("是否为独立插件", pluginDescriptor.isStandalone());

				Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor);

				pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(),
						pluginDescriptor.isStandalone(), pluginDescriptor.getDependencies(), pluginDescriptor.getMuliDexList());
				Context pluginContext = PluginCreator
						.createPluginContext(pluginDescriptor, sApplication, pluginRes, pluginClassLoader);

				//插件Context默认主题设置为插件application主题
				pluginContext.setTheme(pluginDescriptor.getApplicationTheme());
				pluginDescriptor.setPluginContext(pluginContext);
				pluginDescriptor.setPluginClassLoader(pluginClassLoader);

				callPluginApplicationOnCreate(pluginDescriptor);

				try {
					ActivityThread.installPackageInfo(getApplicatoin(), pluginDescriptor.getPackageName(), pluginDescriptor);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				LogUtil.e("初始化插件" + pluginDescriptor.getPackageName() + "完成");
			}
		}
	}

	private static void callPluginApplicationOnCreate(PluginDescriptor pluginDescriptor) {

		Application application = null;

		if (pluginDescriptor.getPluginApplication() == null && pluginDescriptor.getPluginClassLoader() != null) {
			try {
				LogUtil.d("创建插件Application", pluginDescriptor.getApplicationName());
				//阻止自动安装multidex
				try {
					Class mulitDex = pluginDescriptor.getPluginClassLoader().loadClass("android.support.multidex.MultiDex");
					RefInvoker.setFieldObject(null, mulitDex, "IS_VM_MULTIDEX_CAPABLE", true);
				} catch (Exception e) {
				}
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

		//先拿到宿主的crashHandler
		Thread.UncaughtExceptionHandler old = Thread.getDefaultUncaughtExceptionHandler();

		//执行onCreate
		if (application != null) {
			application.onCreate();
		}

		// 再还原宿主的crashHandler，这里之所以需要还原CrashHandler，
		// 是因为如果插件中自己设置了自己的crashHandler（通常是在oncreate中），
		// 会导致当前进程的主线程的handler被意外修改。
		// 如果有多个插件都有设置自己的crashHandler，也会导致混乱
		// 所以这里直接屏蔽掉插件的crashHandler
		//TODO 或许也可以做成消息链进行分发？
		Thread.setDefaultUncaughtExceptionHandler(old);

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
		pluginManager.removeAll();
	}

	public static synchronized void remove(String pluginId) {
		pluginManager.remove(pluginId);
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

		String packageName = intent.getPackage();
		if (packageName == null && intent.getComponent() != null) {
			packageName = intent.getComponent().getPackageName();
		}
		if (packageName != null && !packageName.equals(PluginLoader.getApplicatoin().getPackageName())) {
			PluginDescriptor dp = getPluginDescriptorByPluginId(packageName);
			if (dp != null) {
				List<String> list = dp.matchPlugin(intent, type);
				if (list != null && list.size() > 0) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.addAll(list);
				}
			}
		} else {
			Iterator<PluginDescriptor> itr = getPlugins().iterator();
			while (itr.hasNext()) {
				List<String> list = itr.next().matchPlugin(intent, type);
				if (list != null && list.size() > 0) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.addAll(list);
				}
				if (result != null && type != PluginDescriptor.BROADCAST) {
					break;
				}
			}

		}
		return result;
	}

}
