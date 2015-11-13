package com.plugin.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Window;

import com.plugin.content.PluginActivityInfo;
import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginProviderInfo;
import com.plugin.core.annotation.AnnotationProcessor;
import com.plugin.core.annotation.FragmentContainer;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;
import com.plugin.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginInjector {

	private static final String android_app_ActivityThread = "android.app.ActivityThread";
	private static final String android_app_ActivityThread_currentActivityThread = "currentActivityThread";
	private static final String android_app_ActivityThread_mInstrumentation = "mInstrumentation";
	private static final String android_app_ActivityThread_getHandler = "getHandler";
	private static final String android_app_ActivityThread_installContentProviders = "installContentProviders";

	private static final String android_content_ContextWrapper_mBase = "mBase";

	private static final String android_os_Handler_mCallback = "mCallback";

	private static final String android_app_Activity_mInstrumentation = "mInstrumentation";
	private static final String android_app_Activity_mActivityInfo = "mActivityInfo";

	private static final String android_content_ContextThemeWrapper_attachBaseContext = "attachBaseContext";
	private static final String android_content_ContextThemeWrapper_mResources = "mResources";
	private static final String android_content_ContextThemeWrapper_mTheme = "mTheme";


	/**
	 * 替换宿主程序Application对象的mBase是为了修改它的几个StartActivity、
	 * StartService和SendBroadcast方法
	 */
	static void injectBaseContext(Application application) {
		LogUtil.d("替换宿主程序Application对象的mBase");
		Context base = (Context)RefInvoker.getFieldObject(application, ContextWrapper.class.getName(),
				android_content_ContextWrapper_mBase);
		Context newBase = new PluginBaseContextWrapper(base);
		RefInvoker.setFieldObject(application, ContextWrapper.class.getName(),
				android_content_ContextWrapper_mBase, newBase);
	}

	static Object getActivityThread() {
		// 从ThreadLocal中取出来的
		LogUtil.d("从宿主程序中取出ActivityThread对象备用");
		Object activityThread = RefInvoker.invokeStaticMethod(android_app_ActivityThread,
				android_app_ActivityThread_currentActivityThread,
				(Class[]) null, (Object[]) null);
		return activityThread;
	}

	/**
	 * 注入Instrumentation主要是为了支持Activity
	 */
	static void injectInstrumentation(Object activityThread) {
		// 给Instrumentation添加一层代理，用来实现隐藏api的调用
		LogUtil.d("替换宿主程序Intstrumentation");
		Instrumentation originalInstrumentation = (Instrumentation) RefInvoker.getFieldObject(activityThread,
				android_app_ActivityThread, android_app_ActivityThread_mInstrumentation);
		RefInvoker.setFieldObject(activityThread, android_app_ActivityThread,
				android_app_ActivityThread_mInstrumentation,
				new PluginInstrumentionWrapper(originalInstrumentation));
	}

	static void injectHandlerCallback(Object activityThread) {
		LogUtil.d("向插入宿主程序消息循环插入回调器");
		Handler handler = (Handler) RefInvoker.invokeMethod(activityThread,
				android_app_ActivityThread, android_app_ActivityThread_getHandler,
				(Class[]) null, (Object[]) null);
		RefInvoker.setFieldObject(handler, Handler.class.getName(), android_os_Handler_mCallback,
				new PluginAppTrace(handler));
	}

	static void installContentProviders(Context context, Collection<PluginProviderInfo> pluginProviderInfos) {
		LogUtil.d("安装插件ContentProvider", pluginProviderInfos.size());
		Object activityThread = PluginInjector.getActivityThread();
		if (activityThread != null) {
			ClassLoaderUtil.hackClassLoaderIfNeeded();
			List<ProviderInfo> providers = new ArrayList<ProviderInfo>();
			for (PluginProviderInfo pluginProviderInfo : pluginProviderInfos) {
				ProviderInfo p = new ProviderInfo();
				p.name = pluginProviderInfo.getName();
				p.authority = pluginProviderInfo.getAuthority();
				p.applicationInfo = context.getApplicationInfo();
				p.exported = pluginProviderInfo.isExported();
				p.packageName = context.getApplicationInfo().packageName;
				providers.add(p);
			}
			RefInvoker.invokeMethod(activityThread,
					android_app_ActivityThread, android_app_ActivityThread_installContentProviders,
					new Class[]{Context.class, List.class}, new Object[]{context, providers});
		}
	}

	static void injectInstrumetionFor360Safe(Activity activity, Instrumentation pluginInstrumentation) {
		// 检查mInstrumention是否已经替换成功。
		// 之所以要检查，是因为如果手机上安装了360手机卫士等app，它们可能会劫持用户app的ActivityThread对象，
		// 导致在PluginApplication的onCreate方法里面替换mInstrumention可能会失败
		// 所以这里再做一次检查
		Instrumentation instrumention = (Instrumentation) RefInvoker.getFieldObject(activity, Activity.class.getName(),
				android_app_Activity_mInstrumentation);
		if (!(instrumention instanceof PluginInstrumentionWrapper)) {
			// 说明被360还原了，这里再次尝试替换
			RefInvoker.setFieldObject(activity, Activity.class.getName(), android_app_Activity_mInstrumentation, pluginInstrumentation);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static void injectActivityContext(Activity activity) {
		Intent intent = activity.getIntent();
		FragmentContainer fragmentContainer = AnnotationProcessor.getFragmentContainer(activity.getClass());
		// 如果是打开插件中的activity, 或者是打开的用来显示插件fragment的宿主activity
		if (fragmentContainer != null || (intent.getComponent() != null
				&& (intent.getComponent().getClassName().startsWith(PluginStubBinding.STUB_ACTIVITY_PRE)))) {
			// 为了不需要重写插件Activity的attachBaseContext方法为：
			// 我们在activityoncreate之前去完成attachBaseContext的事情

			Context pluginContext = null;
			PluginDescriptor pd = null;

			//是打开的用来显示插件fragment的宿主activity
			if (fragmentContainer != null) {
				// 为了能够在宿主中的Activiy里面展示来自插件的普通Fragment，
				// 我们将宿主程序中用来展示插件普通Fragment的Activity的Context也替换掉

				if (!TextUtils.isEmpty(fragmentContainer.pluginId())) {

					pd = PluginLoader.getPluginDescriptorByPluginId(fragmentContainer.pluginId());
					pluginContext = PluginLoader.getNewPluginComponentContext(pd.getPluginContext(), activity.getBaseContext());

				} else if (!TextUtils.isEmpty(fragmentContainer.fragmentId())) {
					String classId = null;
					try {
						//TODO
						classId = intent.getStringExtra(fragmentContainer.fragmentId());
					} catch (Exception e) {
						LogUtil.printException("这里的Intent如果包含来自插件的VO对象实例，" +
								"会产生ClassNotFound异常", e);
					}
					if (classId != null) {
						@SuppressWarnings("rawtypes")
						Class clazz = PluginLoader.loadPluginFragmentClassById(classId);

						pd = PluginLoader.getPluginDescriptorByClassName(clazz.getName());
						pluginContext = PluginLoader.getNewPluginComponentContext(pd.getPluginContext(), activity.getBaseContext());

					} else {
						return;
					}
				} else {
					LogUtil.e("FragmentContainer注解至少配置一个参数：pluginId, fragmentId");
					return;
				}

			} else {
				//是打开插件中的activity
				pd = PluginLoader.getPluginDescriptorByClassName(activity.getClass().getName());
				pluginContext = PluginLoader.getNewPluginComponentContext(pd.getPluginContext(), activity.getBaseContext());

				//获取插件Application对象
				Application pluginApp = pd.getPluginApplication();
				if (pluginApp != null) {
					//重设mApplication
					RefInvoker.setFieldObject(activity, Activity.class.getName(),
							"mApplication", pluginApp);
				}

			}

			PluginActivityInfo pluginActivityInfo = pd.getActivityInfos().get(activity.getClass().getName());
			ActivityInfo activityInfo = (ActivityInfo) RefInvoker.getFieldObject(activity, Activity.class.getName(),
					android_app_Activity_mActivityInfo);
			int pluginAppTheme = getPluginTheme(activityInfo, pluginActivityInfo, pd);

			resetActivityContext(pluginContext, activity, pluginAppTheme);

			resetWindowConfig(pluginContext, pd, activity, activityInfo, pluginActivityInfo);

			activity.setTitle(activity.getClass().getName());

		} else {
			// 如果是打开宿主程序的activity，注入一个无害的Context，用来在宿主程序中startService和sendBroadcast时检查打开的对象是否是插件中的对象
			// 插入Context
			Context mainContext = new PluginBaseContextWrapper(activity.getBaseContext());
			RefInvoker.setFieldObject(activity, ContextWrapper.class.getName(), android_content_ContextWrapper_mBase, null);
			RefInvoker.invokeMethod(activity, ContextThemeWrapper.class.getName(), android_content_ContextThemeWrapper_attachBaseContext,
					new Class[]{Context.class}, new Object[]{mainContext});
		}
	}

	static void resetActivityContext(final Context pluginContext, final Activity activity,
									 final int pluginAppTheme) {
		if (pluginContext == null) {
			return;
		}

		// 重设BaseContext
		RefInvoker.setFieldObject(activity, ContextWrapper.class.getName(), android_content_ContextWrapper_mBase, null);
		RefInvoker.invokeMethod(activity, ContextThemeWrapper.class.getName(), android_content_ContextThemeWrapper_attachBaseContext,
				new Class[]{Context.class }, new Object[] { pluginContext });

		// 由于在attach的时候Resource已经被初始化了，所以需要重置Resource
		RefInvoker.setFieldObject(activity, ContextThemeWrapper.class.getName(), android_content_ContextThemeWrapper_mResources, null);

		// 重设theme
		if (pluginAppTheme != 0) {
			RefInvoker.setFieldObject(activity, ContextThemeWrapper.class.getName(), android_content_ContextThemeWrapper_mTheme, null);
			activity.setTheme(pluginAppTheme);
		}
		// 重设theme
		((PluginContextTheme)pluginContext).mTheme = null;
		pluginContext.setTheme(pluginAppTheme);

		//重设mContext
		RefInvoker.setFieldObject(activity.getWindow(), Window.class.getName(),
				"mContext", pluginContext);

		//重设mWindowStyle
		RefInvoker.setFieldObject(activity.getWindow(), Window.class.getName(),
				"mWindowStyle", null);

		// 重设LayoutInflater
		LogUtil.d(activity.getWindow().getClass().getName());
		RefInvoker.setFieldObject(activity.getWindow(), activity.getWindow().getClass().getName(),
				"mLayoutInflater", LayoutInflater.from(pluginContext));

		// 如果api>=11,还要重设factory2
		if (Build.VERSION.SDK_INT >= 11) {
			RefInvoker.invokeMethod(activity.getWindow().getLayoutInflater(), LayoutInflater.class.getName(),
					"setPrivateFactory", new Class[]{LayoutInflater.Factory2.class}, new Object[]{activity});
		}
	}

	static void resetWindowConfig(final Context pluginContext, final PluginDescriptor pd,
								  final Activity activity,
								  final ActivityInfo activityInfo,
								  final PluginActivityInfo pluginActivityInfo) {

		if (pluginActivityInfo != null) {
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
				if (orientation != activityInfo.screenOrientation) {
					//noinspection ResourceType
					activity.setRequestedOrientation(orientation);
				}
			}
			if (Build.VERSION.SDK_INT >= 18) {
				Boolean isImmersive = ResourceUtil.getBoolean(pluginActivityInfo.getImmersive(), pluginContext);
				if (isImmersive != null) {
					activity.setImmersive(isImmersive);
				}
			}

			LogUtil.d(activity.getClass().getName(), "immersive", pluginActivityInfo.getImmersive());
			LogUtil.d(activity.getClass().getName(), "screenOrientation", pluginActivityInfo.getScreenOrientation());
			LogUtil.d(activity.getClass().getName(), "launchMode", pluginActivityInfo.getLaunchMode());
			LogUtil.d(activity.getClass().getName(), "windowSoftInputMode", pluginActivityInfo.getWindowSoftInputMode());
			LogUtil.d(activity.getClass().getName(), "uiOptions", pluginActivityInfo.getUiOptions());
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

	private static int getPluginTheme(ActivityInfo activityInfo, PluginActivityInfo pluginActivityInfo, PluginDescriptor pd) {
		int pluginAppTheme = 0;
		if (pluginActivityInfo != null ) {
			pluginAppTheme = ResourceUtil.getResourceId(pluginActivityInfo.getTheme());
		}
		if (pluginAppTheme == 0) {
			pluginAppTheme = pd.getApplicationTheme();
		}
		if (pluginAppTheme == 0) {
			pluginAppTheme = activityInfo.getThemeResource();
		}
		return pluginAppTheme;
	}
}
