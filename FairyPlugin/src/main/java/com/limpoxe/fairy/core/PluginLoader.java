package com.limpoxe.fairy.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.android.HackLayoutInflater;
import com.limpoxe.fairy.core.compat.CompatForSupportv7ViewInflater;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidAppIActivityManager;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidAppINotificationManager;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidAppIPackageManager;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidOsServiceManager;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidWebkitWebViewFactoryProvider;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class PluginLoader {

	private static Application sApplication;
	private static boolean isLoaderInited = false;
	private static int sLoadingResId;
	private static long sMinLoadingTime = 400;

	private PluginLoader() {
	}

	public static Application getApplication() {
		if (sApplication == null) {
			throw new IllegalStateException("框架尚未初始化，请确定在当前进程中，PluginLoader.initLoader方法已执行！");
		}
		return sApplication;
	}

	/**
	 * 初始化loader, 只可调用一次
	 * 
	 * @param app
	 */
	public static synchronized void initLoader(Application app) {
		if (!isLoaderInited) {
			LogUtil.v("插件框架初始化中...");

			long t1 = System.currentTimeMillis();

			isLoaderInited = true;
			sApplication = app;

			//这里的isPluginProcess方法需要在安装AndroidAppIActivityManager之前执行一次。
			//原因见AndroidAppIActivityManager的getRunningAppProcesses()方法
			boolean isPluginProcess = ProcessUtil.isPluginProcess();

			if(ProcessUtil.isPluginProcess()) {
				AndroidOsServiceManager.installProxy();
			}

			AndroidAppIActivityManager.installProxy();
			AndroidAppINotificationManager.installProxy();
			AndroidAppIPackageManager.installProxy(sApplication.getPackageManager());

			if (isPluginProcess) {
				HackLayoutInflater.installPluginCustomViewConstructorCache();
				CompatForSupportv7ViewInflater.installPluginCustomViewConstructorCache();
				//不可在主进程中同步安装，因为此时ActivityThread还没有准备好, 会导致空指针。
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						AndroidWebkitWebViewFactoryProvider.installProxy();
					}
				});
			}

			PluginInjector.injectHandlerCallback();//本来宿主进程是不需要注入handlecallback的，这里加上是为了对抗360安全卫士等软件，提高Instrumentation的成功率
			PluginInjector.injectInstrumentation();
			PluginInjector.injectBaseContext(sApplication);

			if (isPluginProcess) {
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
							Intent intent = activity.getIntent();
							if (intent != null && intent.getComponent() != null) {
                                LogUtil.v("回收绑定关系");
                                PluginProviderClient.unBindLaunchModeStubActivity(intent.getComponent().getClassName(), activity.getClass().getName());
							}
						}
					});
				}
			}

            removeNotSupportedPluginIfUpgraded();

			long t2 = System.currentTimeMillis();
			LogUtil.w("插件框架初始化完成", "耗时：" + (t2-t1));
		}
	}

    private static void removeNotSupportedPluginIfUpgraded() {
        //如果宿主进行了覆盖安装的升级操作，移除已经安装的对宿主版本有要求的非独立插件
        String KEY = "last_host_versionName";
        SharedPreferences prefs = sApplication.getSharedPreferences("fairy_configs", Context.MODE_PRIVATE);
        String lastHostVersoinName = prefs.getString(KEY, null);
        String hostVersionName = null;
        try {
            PackageManager packageManager = PluginLoader.getApplication().getPackageManager();
            PackageInfo hostPackageInfo = packageManager.getPackageInfo(PluginLoader.getApplication().getPackageName(), PackageManager.GET_META_DATA);
            hostVersionName = hostPackageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        //版本号发生了变化
        if (!hostVersionName.equals(lastHostVersoinName)) {
            //遍历检查已安装的非独立插件是否支持当前版本的宿主
            ArrayList<PluginDescriptor> pluginDescriptorList =  PluginManagerHelper.getPlugins();
            for(int i = 0; i < pluginDescriptorList.size(); i++) {
                PluginDescriptor pluginDescriptor = pluginDescriptorList.get(i);
                if (!pluginDescriptor.isStandalone() && pluginDescriptor.getRequiredHostVersionName() != null) {
                    //是非独立插件，而且指定了插件运行需要的的宿主版本
                    //判断宿主版本是否满足要求
                    if (!pluginDescriptor.getRequiredHostVersionName().equals(hostVersionName)) {
                        //不满足要求，卸载此插件
                        LogUtil.e("当前宿主版本不支持此插件版本", "宿主versionName:" + hostVersionName, "插件RequiredHostVersionName:" + pluginDescriptor.getRequiredHostVersionName());
                        LogUtil.w("卸载此插件");
                        PluginManagerHelper.remove(pluginDescriptor.getPackageName());
                    }
                }
            }
            prefs.edit().putString(KEY, hostVersionName).apply();
        }
    }

	public static Context fixBaseContextForReceiver(Context superApplicationContext) {
		if (superApplicationContext instanceof ContextWrapper) {
			return ((ContextWrapper)superApplicationContext).getBaseContext();
		} else {
			return superApplicationContext;
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
		PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByFragmentId(clazzId);
		if (pluginDescriptor != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);

			DexClassLoader pluginClassLoader = plugin.pluginClassLoader;

			String clazzName = pluginDescriptor.getPluginClassNameById(clazzId);
			if (clazzName != null) {
				try {
					Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
					LogUtil.v("loadPluginClass for clazzId", clazzId, "clazzName", clazzName, "success");
					return pluginClazz;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
			LogUtil.e("未安装插件", clazzId, "fail");
		}
		return null;

	}

	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassByName(String clazzName) {
		PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(clazzName);
		return loadPluginClassByName(pluginDescriptor, clazzName);
	}

	public static Class loadPluginClassByName(PluginDescriptor pluginDescriptor, String clazzName) {

		if (pluginDescriptor != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);

			DexClassLoader pluginClassLoader = plugin.pluginClassLoader;

			try {
				Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
				LogUtil.v("loadPluginClass Success for clazzName ", clazzName);
				return pluginClazz;
			} catch (ClassNotFoundException e) {
				LogUtil.printException("ClassNotFound " + clazzName, e);
			} catch (java.lang.IllegalAccessError illegalAccessError) {
				illegalAccessError.printStackTrace();
				throw new IllegalAccessError("出现这个异常最大的可能是插件dex和" +
						"宿主dex包含了相同的class导致冲突, " +
						"请检查插件的编译脚本，确保排除了所有公共依赖库的jar");
			}

		} else {
			LogUtil.e("loadPluginClass Fail for clazzName ", clazzName, pluginDescriptor==null?"pluginDescriptor = null":"pluginDescriptor not null");
		}

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
		PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(clazz.getName());

		if (pluginDescriptor != null) {
			pluginContext = PluginLauncher.instance().getRunningPlugin(pluginDescriptor.getPackageName()).pluginContext;;
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
	/*package*/ static Context getNewPluginComponentContext(Context pluginContext, Context base, int theme) {
		PluginContextTheme newContext = null;
		if (pluginContext != null) {
			newContext = (PluginContextTheme)PluginCreator.createPluginContext(((PluginContextTheme) pluginContext).getPluginDescriptor(),
					base, pluginContext.getResources(),
					(DexClassLoader) pluginContext.getClassLoader());

			newContext.setPluginApplication((Application) ((PluginContextTheme) pluginContext).getApplicationContext());

			newContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		}
		return newContext;
	}

	public static Context getNewPluginApplicationContext(String pluginId) {
		PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);

		//插件可能尚未初始化，确保使用前已经初始化
		LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);

		if (plugin != null) {
			PluginContextTheme newContext = (PluginContextTheme)PluginCreator.createPluginContext(
					((PluginContextTheme) plugin.pluginContext).getPluginDescriptor(),
					sApplication.getBaseContext(), plugin.pluginResource, plugin.pluginClassLoader);

			newContext.setPluginApplication(plugin.pluginApplication);

			newContext.setTheme(pluginDescriptor.getApplicationTheme());

			return newContext;
		}

		return null;
	}

    /**
     * 首次打开插件时，如果是通过Activity打开，会显示一个空白loading页，
     * 通过resId设置loading页ui
     * @param resId
     */
	public static void setLoadingResId(int resId) {
		sLoadingResId = resId;
	}

	public static int getLoadingResId() {
		return sLoadingResId;
	}

    /**
     * 设置loading页最小等待时间，用于在插件较简单，初始化较快时，避免loading页一闪而过
     * 时间设置为0表示无loading页
     * @param minLoadingTime
     */
	public static void setMinLoadingTime(long minLoadingTime) {
		sMinLoadingTime = minLoadingTime;
	}

	public static long getMinLoadingTime() {
		return sMinLoadingTime;
	}

}
