package com.limpoxe.fairy.core;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Window;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginActivityInfo;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.content.PluginProviderInfo;
import com.limpoxe.fairy.core.android.HackActivity;
import com.limpoxe.fairy.core.android.HackActivityThread;
import com.limpoxe.fairy.core.android.HackApplication;
import com.limpoxe.fairy.core.android.HackContextImpl;
import com.limpoxe.fairy.core.android.HackContextThemeWrapper;
import com.limpoxe.fairy.core.android.HackContextWrapper;
import com.limpoxe.fairy.core.android.HackLayoutInflater;
import com.limpoxe.fairy.core.android.HackLoadedApk;
import com.limpoxe.fairy.core.android.HackService;
import com.limpoxe.fairy.core.android.HackWindow;
import com.limpoxe.fairy.core.annotation.AnnotationProcessor;
import com.limpoxe.fairy.core.annotation.PluginContainer;
import com.limpoxe.fairy.core.compat.CompatForSupportv7_23_2;
import com.limpoxe.fairy.core.exception.PluginNotFoundError;
import com.limpoxe.fairy.core.exception.PluginNotInitError;
import com.limpoxe.fairy.core.loading.WaitForLoadingPluginActivity;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginManagerProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import com.limpoxe.fairy.core.compat.CompatForAppComponentFactoryApi28;

public class PluginInjector {

	/**
	 * 替换宿主程序Application对象的mBase是为了修改它的几个StartActivity、
	 * StartService和SendBroadcast方法
	 */
	static void injectBaseContext(Context context) {
		LogUtil.v("替换宿主程序Application对象的mBase");
		HackContextWrapper wrapper = new HackContextWrapper(context);
		wrapper.setBase(new PluginBaseContextWrapper(wrapper.getBase()));
	}

	/**
	 * 注入Instrumentation主要是为了支持Activity
	 */
	static void injectInstrumentation() {
		// 给Instrumentation添加一层代理，用来实现隐藏api的调用
		LogUtil.d("替换宿主程序Intstrumentation");
		HackActivityThread.wrapInstrumentation();
	}

	static void injectHandlerCallback() {
		LogUtil.v("向宿主程序消息循环插入回调器");
		HackActivityThread.wrapHandler();
	}

	public static void installContentProviders(Context context, Context pluginContext, Collection<PluginProviderInfo> pluginProviderInfos) {
        ProviderInfo[] hostProviders = new ProviderInfo[0];
		try {
			hostProviders = FairyGlobal.getHostApplication().getPackageManager()
				.getPackageInfo(FairyGlobal.getHostApplication().getPackageName(),
					PackageManager.GET_PROVIDERS).providers;
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean isAlreadyAddByHost = false;
		List<ProviderInfo> providers = new ArrayList<ProviderInfo>();
		for (PluginProviderInfo pluginProviderInfo : pluginProviderInfos) {

		    isAlreadyAddByHost = false;

			if (hostProviders != null) {
				for(ProviderInfo hostProvider : hostProviders) {
					if (hostProvider.authority.equals(pluginProviderInfo.getAuthority())) {
						LogUtil.e("此contentProvider已经在宿主中定义，不再安装插件中定义的contentprovider", hostProvider.authority, pluginProviderInfo.getName(), pluginProviderInfo.getName());
						isAlreadyAddByHost = true;
						break;
					}
				}
			}
			if (isAlreadyAddByHost) {
				continue;
			}

			ProviderInfo p = new ProviderInfo();
			p.name = pluginProviderInfo.getName();
			p.authority = pluginProviderInfo.getAuthority();
			p.applicationInfo = new ApplicationInfo(context.getApplicationInfo());
			p.applicationInfo.packageName = pluginContext.getPackageName();
			p.exported = pluginProviderInfo.isExported();
			p.packageName = context.getApplicationInfo().packageName;
			p.grantUriPermissions = pluginProviderInfo.isGrantUriPermissions();
			providers.add(p);
		}

		if(providers.size() > 0) {
			LogUtil.e("为插件安装ContentProvider", pluginContext.getPackageName(), pluginProviderInfos.size());
			//安装的时候使用的是插件的Context, 所有无需对Classloader进行映射处理
			//todo
			HackActivityThread.get().installContentProviders(pluginContext, providers);
		}
	}

	static void injectInstrumetionFor360Safe(Activity activity, Instrumentation pluginInstrumentation) {
		// 检查mInstrumention是否已经替换成功。
		// 之所以要检查，是因为如果手机上安装了360手机卫士等app，它们可能会劫持用户app的ActivityThread对象，
		// 导致在PluginApplication的onCreate方法里面替换mInstrumention可能会失败
		// 所以这里再做一次检查
		HackActivity hackActivity = new HackActivity(activity);
		Instrumentation instrumention = hackActivity.getInstrumentation();
		if (!(instrumention instanceof PluginInstrumentionWrapper)) {
			// 说明被360还原了，这里再次尝试替换
			hackActivity.setInstrumentation(pluginInstrumentation);
		}
	}

	static void injectActivityContext(final Activity activity) {
		if (activity instanceof WaitForLoadingPluginActivity) {
			return;
		}
		if (activity instanceof RealHostClassLoader.TolerantActivity) {
			return;
		}

		LogUtil.v("injectActivityContext");

		PluginContainer container = null;
		boolean isStubActivity = false;

		if (ProcessUtil.isPluginProcess()) {
			// 如果是打开插件中的activity,
			Intent intent = activity.getIntent();
			isStubActivity = PluginManagerProviderClient.isStub(intent.getComponent().getClassName());

			// 或者是打开的用来显示插件组件的宿主activity
			container = AnnotationProcessor.getPluginContainer(activity.getClass());
		}

		HackActivity hackActivity = new HackActivity(activity);

		if (isStubActivity || container != null) {

			// 在activityoncreate之前去完成attachBaseContext的事情

			Context pluginContext = null;
			PluginDescriptor pluginDescriptor = null;

			if (isStubActivity) {
				//是打开插件中的activity

				pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(activity.getClass().getName());
				if(pluginDescriptor == null) {
                    throw new PluginNotFoundError("未找到插件：" + activity.getClass().getName() + ", 插件未安装、或正在安装、或已损坏");
                }

				LoadedPlugin plugin = PluginLauncher.instance().getRunningPlugin(pluginDescriptor.getPackageName());
				if (plugin == null) {
					throw new PluginNotInitError("插件尚未初始化 " + pluginDescriptor.getPackageName() + " " + plugin);
				}

				pluginContext = PluginCreator.createNewPluginComponentContext(plugin.pluginContext, activity.getBaseContext(), 0);

				//获取插件Application对象
				Application pluginApp = plugin.pluginApplication;

				//重设mApplication
				hackActivity.setApplication(pluginApp);
			} else {

				//是打开的用来显示插件组件的宿主activity, 比如在宿主Activity中显示插件Fragment或者插件View

                String pluginId = container.pluginId();
				if (!TextUtils.isEmpty(pluginId)) {
					//进入这里表示指定了这个宿主Activity "只显示" 某个插件的组件
					// 因此直接将这个Activity的Context也替换成插件的Context
					pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
					if(pluginDescriptor == null) {
						throw new PluginNotFoundError("未找到插件：" + pluginId + ", 插件未安装、或正在安装、或已损坏");
					}

					//插件可能尚未初始化，确保使用前已经初始化
					LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);
					if (plugin == null) {
						throw new PluginNotInitError("启动插件失败 " + pluginDescriptor.getPackageName());
					}
					pluginContext = PluginCreator.createNewPluginComponentContext(plugin.pluginContext, activity.getBaseContext(), 0);

				} else {
					//进入这里表示这个宿主可能要同时显示来自多个不同插件的组件, 也就没办法将Context替换成之中某一个插件的context,
					//如果多个不同插件的组件是通过PluginView标签添加的，则会通过注入PluginViewFactory去处理Classloader

					//这一行是为了配合RealHostClassLoader解决在宿主Activity被系统自动恢复时同时自动恢复了来自插件的Fragment而产生的ClassNotFound问题
					PluginInjector.hackHostClassLoaderIfNeeded();

					//不管怎样，如果打开的是宿主的Activity，都需要注入一个Context，用来在宿主中startActivity和sendBroadcast时检查目标是否为插件组件
					Context mainContext = new PluginBaseContextWrapper(activity.getBaseContext());
					hackActivity.setBase(null);
					hackActivity.attachBaseContext(mainContext);
					return;
				}

			}

			PluginActivityInfo pluginActivityInfo = pluginDescriptor.getActivityInfos().get(activity.getClass().getName());

			ActivityInfo activityInfo = hackActivity.getActivityInfo();
			int pluginAppTheme = getPluginTheme(activityInfo, pluginActivityInfo, pluginDescriptor);

			LogUtil.e("Theme", "0x" + Integer.toHexString(pluginAppTheme), activity.getClass().getName());

            //pluginActivityInfo != null的判断是为了避免在Fragment插件嵌入其他Activity时没有pluginActivityInfo造成NPE
            if (pluginActivityInfo != null && pluginActivityInfo.isUseHostPackageName()) {
                LogUtil.e("useHostPackageName true");
                ((PluginContextTheme)pluginContext).setUseHostPackageName(true);
            }

			resetActivityContext(pluginContext, activity, pluginAppTheme);

            //如果是配置了PluginContainer注解和pluginId的宿主Activity，此宿主的Activity的全屏配置可能会被插件的主题覆盖而丢失，可以通过代码设置回去
            resetWindowConfig(pluginContext, pluginDescriptor, activity, activityInfo, pluginActivityInfo);

            String simpleName = activity.getClass().getSimpleName();
			activity.setTitle(simpleName!=null?simpleName:activity.getClass().getName());

		} else {
			// 如果是打开宿主程序的activity，注入一个无害的Context，用来在宿主程序中startService和sendBroadcast时检查打开的对象是否是插件中的对象
			// 插入Context
			Context mainContext = new PluginBaseContextWrapper(activity.getBaseContext());
			hackActivity.setBase(null);
			hackActivity.attachBaseContext(mainContext);
		}
	}

	static void resetActivityContext(final Context pluginContext, final Activity activity,
									 final int pluginAppTheme) {
		if (pluginContext == null) {
			return;
		}

		// 重设BaseContext
		HackContextThemeWrapper hackContextThemeWrapper = new HackContextThemeWrapper(activity);
		hackContextThemeWrapper.setBase(null);
		hackContextThemeWrapper.attachBaseContext(pluginContext);

		// 由于在attach的时候Resource已经被初始化了，所以需要重置Resource
		hackContextThemeWrapper.setResources(null);

		CompatForSupportv7_23_2.fixResource(pluginContext, activity);

		// 重设theme
		if (pluginAppTheme != 0) {
			hackContextThemeWrapper.setTheme(null);
			activity.setTheme(pluginAppTheme);
		}
		// 重设theme
		((PluginContextTheme)pluginContext).mTheme = null;
		pluginContext.setTheme(pluginAppTheme);

		Window window = activity.getWindow();

		HackWindow hackWindow = new HackWindow(window);
		//重设mContext
		hackWindow.setContext(pluginContext);

		//重设mWindowStyle
		hackWindow.setWindowStyle(null);

		// 重设LayoutInflater
		LogUtil.v(window.getClass().getName());
		//注意：这里getWindow().getClass().getName() 不一定是android.view.Window
		//如miui下返回MIUI window
		hackWindow.setLayoutInflater(window.getClass().getName(), LayoutInflater.from(activity));

		// 如果api>=11,还要重设factory2
		if (Build.VERSION.SDK_INT >= 11) {
			new HackLayoutInflater(window.getLayoutInflater()).setPrivateFactory(activity);
		}
	}

	static void resetWindowConfig(final Context pluginContext, final PluginDescriptor pd,
								  final Activity activity,
								  final ActivityInfo activityInfo,
								  final PluginActivityInfo pluginActivityInfo) {

		if (pluginActivityInfo != null) {

			//如果PluginContextTheme的getPackageName返回了插件包名,需要在这里对attribute修正
			activity.getWindow().getAttributes().packageName = FairyGlobal.getHostApplication().getPackageName();

			if (null != pluginActivityInfo.getWindowSoftInputMode()) {
				activity.getWindow().setSoftInputMode((int)Long.parseLong(pluginActivityInfo.getWindowSoftInputMode().replace("0x", ""), 16));
			}
			if (Build.VERSION.SDK_INT >= 14) {
				if (null != pluginActivityInfo.getUiOptions()) {
					activity.getWindow().setUiOptions((int)Long.parseLong(pluginActivityInfo.getUiOptions().replace("0x", ""), 16));
				}
			}
			if (null != pluginActivityInfo.getScreenOrientation()) {
				int orientation = (int)Long.parseLong(pluginActivityInfo.getScreenOrientation());
				//noinspection ResourceType
				if (orientation != activityInfo.screenOrientation && !activity.isChild()) {
					//noinspection ResourceType
                    //框架中只内置了unspec和landscape两种screenOrientation
                    //如果是其他类型，这里通过代码实现切换
                    LogUtil.v("修改screenOrientation");
					activity.setRequestedOrientation(orientation);
				}
			}
			if (Build.VERSION.SDK_INT >= 18 && !activity.isChild()) {
				Boolean isImmersive = ResourceUtil.getBoolean(pluginActivityInfo.getImmersive(), pluginContext);
				if (isImmersive != null) {
					activity.setImmersive(isImmersive);
				}
			}

			String activityClassName = activity.getClass().getName();
			LogUtil.v(activityClassName, "immersive", pluginActivityInfo.getImmersive());
			LogUtil.v(activityClassName, "screenOrientation", pluginActivityInfo.getScreenOrientation());
			LogUtil.v(activityClassName, "launchMode", pluginActivityInfo.getLaunchMode());
			LogUtil.v(activityClassName, "windowSoftInputMode", pluginActivityInfo.getWindowSoftInputMode());
			LogUtil.v(activityClassName, "uiOptions", pluginActivityInfo.getUiOptions());
		}

		//如果是独立插件，由于没有合并资源，这里还需要替换掉 mActivityInfo，
		//避免activity试图通过ActivityInfo中的资源id来读取资源时失败
		activityInfo.icon = pd.getApplicationIcon();
		activityInfo.logo = pd.getApplicationLogo();
		if (Build.VERSION.SDK_INT >= 19) {
			activity.getWindow().setIcon(activityInfo.icon);
			activity.getWindow().setLogo(activityInfo.logo);
		}
	}

	/*package*/static void replaceReceiverContext(Context baseContext, Context newBase) {

		if (HackContextImpl.instanceOf(baseContext)) {
			ContextWrapper receiverRestrictedContext = new HackContextImpl(baseContext).getReceiverRestrictedContext();
			new HackContextWrapper(receiverRestrictedContext).setBase(newBase);
		}
	}

	//这里是因为在多进程情况下，杀死插件进程，自动恢复service时有个bug导致一个service同时存在多个service实例
	//这里做个遍历保护
	//break;
	/*package*/static void replacePluginServiceContext(String serviceName) {
		Map<IBinder, Service> services = HackActivityThread.get().getServices();
		if (services != null) {
			Iterator<Service> itr = services.values().iterator();
			while(itr.hasNext()) {
				Service service = itr.next();
				if (service != null && service.getClass().getName().equals(serviceName) ) {

					replacePluginServiceContext(serviceName,service );
				}

			}
		}
	}

	public static void replacePluginServiceContext(String servieName, Service service) {
		PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByClassName(servieName);

		LoadedPlugin plugin = PluginLauncher.instance().getRunningPlugin(pd.getPackageName());

		HackService hackService = new HackService(service);
		hackService.setBase(
				PluginCreator.createNewPluginComponentContext(plugin.pluginContext,
						service.getBaseContext(), pd.getApplicationTheme()));
		hackService.setApplication(plugin.pluginApplication);
		hackService.setClassName(PluginManagerProviderClient.bindStubService(service.getClass().getName()));

	}

	/*package*/static void replaceHostServiceContext(String serviceName) {
		Map<IBinder, Service> services = HackActivityThread.get().getServices();
		if (services != null) {
			Iterator<Service> itr = services.values().iterator();
			while(itr.hasNext()) {
				Service service = itr.next();
				if (service != null && service.getClass().getName().equals(serviceName) ) {
					PluginInjector.injectBaseContext(service);
					break;
				}

			}
		}
	}

	/**
	 * 主题的选择顺序为 先选择插件Activity配置的主题，再选择插件Application配置的主题，
	 * 如果是非独立插件，再选择宿主Activity主题
	 * 如果是独立插件，再选择系统默认主题
	 * @param activityInfo
	 * @param pluginActivityInfo
	 * @param pd
	 * @return
	 */
	private static int getPluginTheme(ActivityInfo activityInfo, PluginActivityInfo pluginActivityInfo, PluginDescriptor pd) {
		int pluginAppTheme = 0;
		if (pluginActivityInfo != null ) {
			pluginAppTheme = ResourceUtil.parseResId(pluginActivityInfo.getTheme());
		}
		if (pluginAppTheme == 0) {
			pluginAppTheme = pd.getApplicationTheme();
		}

		if (pluginAppTheme == 0 && pd.isStandalone()) {
			pluginAppTheme = android.R.style.Theme_DeviceDefault;
		}

		if (pluginAppTheme == 0) {
			//If the activity defines a theme, that is used; else, the application theme is used.
			pluginAppTheme = activityInfo.getThemeResource();
		}
		return pluginAppTheme;
	}

	/**
	 * 如果插件中不包含service、receiver，是不需要替换classloader的
	 */
	public static void hackHostClassLoaderIfNeeded() {
        LogUtil.v("hackHostClassLoaderIfNeeded");

        HackApplication hackApplication = new HackApplication(FairyGlobal.getHostApplication());
		Object mLoadedApk = hackApplication.getLoadedApk();
		if (mLoadedApk == null) {
			//重试一次
			mLoadedApk = hackApplication.getLoadedApk();
		}
		if(mLoadedApk == null) {
			//换个方式再试一次
			mLoadedApk = HackActivityThread.getLoadedApk();
		}
		if (mLoadedApk != null) {
			HackLoadedApk hackLoadedApk = new HackLoadedApk(mLoadedApk);
			ClassLoader originalLoader = hackLoadedApk.getClassLoader();
			if (!(originalLoader instanceof HostClassLoader)) {
				HostClassLoader newLoader = new HostClassLoader("", new RealHostClassLoader("",
						FairyGlobal.getHostApplication().getCacheDir().getAbsolutePath(),/**这里这两个目录参数无实际意义**/
						FairyGlobal.getHostApplication().getCacheDir().getAbsolutePath(),/**这里这两个目录参数无实际意义**/
						originalLoader));
				hackLoadedApk.setClassLoader(newLoader);
			}
		} else {
			LogUtil.e("What!!Why?");
		}
	}

	public static void injectAppComponentFactory() {
		if (Build.VERSION.SDK_INT < 28) {
			return;
		}
		LogUtil.v("hackHostClassLoaderIfNeeded");

		HackApplication hackApplication = new HackApplication(FairyGlobal.getHostApplication());
		Object mLoadedApk = hackApplication.getLoadedApk();
		if (mLoadedApk == null) {
			//重试一次
			mLoadedApk = hackApplication.getLoadedApk();
		}
		if(mLoadedApk == null) {
			//换个方式再试一次
			mLoadedApk = HackActivityThread.getLoadedApk();
		}
		if (mLoadedApk != null) {
			HackLoadedApk hackLoadedApk = new HackLoadedApk(mLoadedApk);
			//Android-P提供了组件钩子，用来拓展组件初始化流程
			//hackLoadedApk.setAppComponentFactory(new CompatForAppComponentFactoryApi28(hackLoadedApk.getAppComponentFactory()));
		} else {
			LogUtil.e("What!!Why?");
		}
	}

	/**
	 * 将当前进程的resource替换掉，适用于宿主资源热修复以及所有插件和宿主的资源完全融合的插件方案
	 * @param context
	 * @param newResourceFile
	 */
	public static void replaceResource(Context context, String newResourceFile) {
		if (newResourceFile == null || context == null) {
			return;
		}

		try {
			Class activityThread_clazz = Class.forName("android.app.ActivityThread");
			Class loadedApk_clazz = null;
			try {
				loadedApk_clazz = Class.forName("android.app.LoadedApk");
			} catch (ClassNotFoundException exception) {
				loadedApk_clazz = Class.forName("android.app.ActivityThread$PackageInfo");
			}
			Field resDir = findField(loadedApk_clazz, "mResDir");
			Field publicSourceDirField = findField(ApplicationInfo.class, "publicSourceDir");
			Field packagesField = findField(activityThread_clazz, "mPackages");
			Field[] packagesFields = null;
			if (Build.VERSION.SDK_INT < 27) {
				packagesFields = new Field[]{packagesField, findField(activityThread_clazz, "mResourcePackages")};
			} else {
				packagesFields = new Field[]{packagesField};
			}

			Object activityThread = getActivityThread(context, activityThread_clazz);
			ApplicationInfo appInfo = context.getApplicationInfo();

			for(int i = 0; i < packagesFields.length; ++i) {
				Object resourcesManager = packagesFields[i].get(activityThread);
				Iterator iterator = ((Map)resourcesManager).entrySet().iterator();
				while(iterator.hasNext()) {
					Map.Entry<String, WeakReference<?>> entry = (Map.Entry)iterator.next();
					Object resourceImpl = ((WeakReference)entry.getValue()).get();
					if (resourceImpl != null) {
						String resDirPath = (String)resDir.get(resourceImpl);
						if (appInfo.sourceDir.equals(resDirPath)) {
							resDir.set(resourceImpl, newResourceFile);
						}
					}
				}
			}

			AssetManager newAssetManager = (AssetManager)AssetManager.class.getConstructor().newInstance();
			Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
			mAddAssetPath.setAccessible(true);
			Integer cookie = (Integer)mAddAssetPath.invoke(newAssetManager, newResourceFile);
			if (cookie == null || cookie == 0) {
				throw new IllegalMonitorStateException();
			}

			try {
				Method mEnsureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
				mEnsureStringBlocks.setAccessible(true);
				mEnsureStringBlocks.invoke(newAssetManager);
			} catch (Exception e) {
			}

			Collection references;
			if (Build.VERSION.SDK_INT >= 19) {
				Class<?> resourcesManager_clazz = Class.forName("android.app.ResourcesManager");
				Method mGetInstance = resourcesManager_clazz.getDeclaredMethod("getInstance");
				mGetInstance.setAccessible(true);
				Object resourcesManager = mGetInstance.invoke((Object)null);
				try {
					Field mAssets = resourcesManager_clazz.getDeclaredField("mActiveResources");
					mAssets.setAccessible(true);
					Map<?, WeakReference<Resources>> map = (Map)mAssets.get(resourcesManager);
					references = map.values();
				} catch (NoSuchFieldException e) {
					Field mResourcesImpl = resourcesManager_clazz.getDeclaredField("mResourceReferences");
					mResourcesImpl.setAccessible(true);
					references = (Collection)mResourcesImpl.get(resourcesManager);
				}
			} else {
				Field mAssets = activityThread_clazz.getDeclaredField("mActiveResources");
				mAssets.setAccessible(true);
				Map<?, WeakReference<Resources>> map = (Map)mAssets.get(activityThread);
				references = map.values();
			}

			Iterator iterator = references.iterator();
			while(iterator.hasNext()) {
				WeakReference<Resources> weakReference = (WeakReference)iterator.next();
				Resources resources = (Resources)weakReference.get();
				if (resources != null) {
					try {
						Field mAssets = Resources.class.getDeclaredField("mAssets");
						mAssets.setAccessible(true);
						mAssets.set(resources, newAssetManager);
					} catch (Throwable throwable) {
						Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
						mResourcesImpl.setAccessible(true);
						Object resourceImpl = mResourcesImpl.get(resources);
						Field mAssets = findField(resourceImpl.getClass(), "mAssets");
						mAssets.setAccessible(true);
						mAssets.set(resourceImpl, newAssetManager);
					}
					resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
				}
			}

			if (Build.VERSION.SDK_INT >= 24) {
				try {
					if (publicSourceDirField != null) {
						publicSourceDirField.set(context.getApplicationInfo(), newResourceFile);
					}
				} catch (Throwable throwable) {
				}
			}
		} catch (Throwable throwable) {
			throw new IllegalStateException(throwable);
		}
	}

	/**
	 * 清理resource静态资源缓存
	 * @param resources
	 */
	public static void refreshResourceCaches(Object resources) {
		if (Build.VERSION.SDK_INT >= 21) {
			try {
				Field mTypedArrayPool = Resources.class.getDeclaredField("mTypedArrayPool");
				mTypedArrayPool.setAccessible(true);
				Object arrayPool = mTypedArrayPool.get(resources);
				Class<?> poolClass = arrayPool.getClass();
				Method acquireMethod = poolClass.getDeclaredMethod("acquire");
				acquireMethod.setAccessible(true);

				Object typedArray;
				do {
					typedArray = acquireMethod.invoke(arrayPool);
				} while(typedArray != null);
			} catch (Throwable throwable) {
			}
		}

		if (Build.VERSION.SDK_INT >= 23) {
			try {
				Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
				mResourcesImpl.setAccessible(true);
				resources = mResourcesImpl.get(resources);
			} catch (Throwable throwable) {
			}
		}

		Object lock = null;
		Field field;
		if (Build.VERSION.SDK_INT >= 18) {
			try {
				field = resources.getClass().getDeclaredField("mAccessLock");
				field.setAccessible(true);
				lock = field.get(resources);
			} catch (Throwable throwable) {
			}
		} else {
			try {
				field = Resources.class.getDeclaredField("mTmpValue");
				field.setAccessible(true);
				lock = field.get(resources);
			} catch (Throwable throwable) {
			}
		}

		if (lock == null) {
			lock = PluginInjector.class;
		}

		synchronized(lock) {
			refreshResourceCache(resources, "mDrawableCache");
			refreshResourceCache(resources, "mColorDrawableCache");
			refreshResourceCache(resources, "mColorStateListCache");
			if (Build.VERSION.SDK_INT >= 23) {
				refreshResourceCache(resources, "mAnimatorCache");
				refreshResourceCache(resources, "mStateListAnimatorCache");
			} else if (Build.VERSION.SDK_INT == 19) {
				refreshResourceCache(resources, "sPreloadedDrawables");
				refreshResourceCache(resources, "sPreloadedColorDrawables");
				refreshResourceCache(resources, "sPreloadedColorStateLists");
			}

		}
	}

	private static boolean refreshResourceCache(Object resources, String fieldName) {
		try {
			Field cacheField = findField(resources.getClass(), fieldName);
			if (cacheField == null) {
				cacheField = Resources.class.getDeclaredField(fieldName);
			}

			cacheField.setAccessible(true);
			Object cache = cacheField.get(resources);
			Class<?> type = cacheField.getType();
			if (Build.VERSION.SDK_INT < 16) {
				if (cache instanceof SparseArray) {
					((SparseArray)cache).clear();
					return true;
				}
			} else {
				Method clearSparseMap;
				if (Build.VERSION.SDK_INT < 23) {
					if ("mColorStateListCache".equals(fieldName)) {
						if (cache instanceof LongSparseArray) {
							((LongSparseArray)cache).clear();
						}
					} else {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
							if (type.isAssignableFrom(ArrayMap.class)) {
								clearSparseMap = Resources.class.getDeclaredMethod("clearDrawableCachesLocked", ArrayMap.class, Integer.TYPE);
								clearSparseMap.setAccessible(true);
								clearSparseMap.invoke(resources, cache, -1);
								return true;
							}
						}

						if (type.isAssignableFrom(LongSparseArray.class)) {
							try {
								clearSparseMap = Resources.class.getDeclaredMethod("clearDrawableCachesLocked", LongSparseArray.class, Integer.TYPE);
								clearSparseMap.setAccessible(true);
								clearSparseMap.invoke(resources, cache, -1);
								return true;
							} catch (NoSuchMethodException e) {
								if (cache instanceof LongSparseArray) {
									((LongSparseArray)cache).clear();
									return true;
								}
							}
						} else if (type.isArray() && type.getComponentType().isAssignableFrom(LongSparseArray.class)) {
							LongSparseArray[] arrays = (LongSparseArray[])((LongSparseArray[])cache);
							for(int i = 0; i < arrays.length; ++i) {
								LongSparseArray array = arrays[i];
								if (array != null) {
									array.clear();
								}
							}

							return true;
						}
					}
				} else {
					while(type != null) {
						try {
							clearSparseMap = type.getDeclaredMethod("onConfigurationChange", Integer.TYPE);
							clearSparseMap.setAccessible(true);
							clearSparseMap.invoke(cache, -1);
							return true;
						} catch (Throwable throwable) {
							type = type.getSuperclass();
						}
					}
				}
			}
		} catch (Throwable throwable) {
		}

		return false;
	}

	private static Object getActivityThread(Context context, Class<?> activityThread_clazz) {
		try {
			Method currentActivityThread_method = activityThread_clazz.getMethod("currentActivityThread");
			currentActivityThread_method.setAccessible(true);
			Object currentActivityThread = currentActivityThread_method.invoke((Object)null);
			if (currentActivityThread != null) {
				return currentActivityThread;
			}
			if (context != null) {
				Field mLoadedApk_field = context.getClass().getField("mLoadedApk");
				mLoadedApk_field.setAccessible(true);
				Object mLoadedApk = mLoadedApk_field.get(context);
				Field mActivityThread_field = mLoadedApk.getClass().getDeclaredField("mActivityThread");
				mActivityThread_field.setAccessible(true);
				return mActivityThread_field.get(mLoadedApk);
			}
		} catch (Throwable throwable) {
		}
		return null;
	}

	private static Field findField(Class clazz, String fieldName) {
		if (clazz == null || fieldName == null) {
			return null;
		}
		Class localClass = clazz;
		while(localClass != Object.class) {
			try {
				Field field = localClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				localClass = localClass.getSuperclass();
			}
		}
		return null;
	}
}
