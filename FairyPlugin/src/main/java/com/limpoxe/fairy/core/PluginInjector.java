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
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
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
import com.limpoxe.fairy.manager.PluginProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		List<ProviderInfo> providers = new ArrayList<ProviderInfo>();
		for (PluginProviderInfo pluginProviderInfo : pluginProviderInfos) {
			ProviderInfo p = new ProviderInfo();
			p.name = pluginProviderInfo.getName();
			p.authority = pluginProviderInfo.getAuthority();
			p.applicationInfo = new ApplicationInfo(context.getApplicationInfo());
			p.applicationInfo.packageName = pluginContext.getPackageName();
			p.exported = pluginProviderInfo.isExported();
			p.packageName = context.getApplicationInfo().packageName;
			providers.add(p);
		}

		if(providers.size() > 0) {
			LogUtil.v("为插件安装ContentProvider", pluginContext.getPackageName(), pluginProviderInfos.size());
			//pluginContext.getPackageName().equals(applicationInfo.packageName) == true
			//安装的时候使用的是插件的Context, 所有无需对Classloader进行映射处理
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

		LogUtil.v("injectActivityContext");

		PluginContainer container = null;
		boolean isStubActivity = false;

		if (ProcessUtil.isPluginProcess()) {
			// 如果是打开插件中的activity,
			Intent intent = activity.getIntent();
			isStubActivity = PluginProviderClient.isStub(intent.getComponent().getClassName());

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
                    throw new PluginNotFoundError("未找到插件：" + activity.getClass().getName() + ", 插件未安装或已损坏");
                }

				LoadedPlugin plugin = PluginLauncher.instance().getRunningPlugin(pluginDescriptor.getPackageName());
				if (plugin == null || plugin.pluginApplication == null) {
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
						throw new PluginNotFoundError("未找到插件：" + pluginId + ", 插件未安装或已损坏");
					}

					//插件可能尚未初始化，确保使用前已经初始化
					LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);
					pluginContext = PluginCreator.createNewPluginComponentContext(plugin.pluginContext, activity.getBaseContext(), 0);

				} else {
					//do nothing
					//进入这里表示这个宿主可能要同时显示来自多个不同插件的组件, 也就没办法将Context替换成之中某一个插件的context,
					//剩下的交给PluginViewFactory去处理
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

			activity.setTitle(activity.getClass().getName());

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
			activity.getWindow().getAttributes().packageName = FairyGlobal.getApplication().getPackageName();

			if (null != pluginActivityInfo.getWindowSoftInputMode()) {
				activity.getWindow().setSoftInputMode(Integer.parseInt(pluginActivityInfo.getWindowSoftInputMode().replace("0x", ""), 16));
			}
			if (Build.VERSION.SDK_INT >= 14) {
				if (null != pluginActivityInfo.getUiOptions()) {
					activity.getWindow().setUiOptions(Integer.parseInt(pluginActivityInfo.getUiOptions().replace("0x", ""), 16));
				}
			}
			if (null != pluginActivityInfo.getScreenOrientation()) {
				int orientation = Integer.parseInt(pluginActivityInfo.getScreenOrientation());
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

	/*package*/static void replacePluginServiceContext(String servieName, Service service) {
		PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByClassName(servieName);

		LoadedPlugin plugin = PluginLauncher.instance().getRunningPlugin(pd.getPackageName());

		HackService hackService = new HackService(service);
		hackService.setBase(
				PluginCreator.createNewPluginComponentContext(plugin.pluginContext,
						service.getBaseContext(), pd.getApplicationTheme()));
		hackService.setApplication(plugin.pluginApplication);
		hackService.setClassName(PluginProviderClient.bindStubService(service.getClass().getName()));

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

        HackApplication hackApplication = new HackApplication(FairyGlobal.getApplication());
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
				HostClassLoader newLoader = new HostClassLoader("",
						FairyGlobal.getApplication()
						.getCacheDir().getAbsolutePath(),
						FairyGlobal.getApplication().getCacheDir().getAbsolutePath(),
						originalLoader);
				hackLoadedApk.setClassLoader(newLoader);
			}
		} else {
			LogUtil.e("What!!Why?");
		}
	}
}
