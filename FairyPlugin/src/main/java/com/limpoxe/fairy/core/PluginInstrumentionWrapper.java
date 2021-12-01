package com.limpoxe.fairy.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.android.HackContextImpl;
import com.limpoxe.fairy.core.android.HackInstrumentation;
import com.limpoxe.fairy.core.annotation.AnnotationProcessor;
import com.limpoxe.fairy.core.annotation.PluginContainer;
import com.limpoxe.fairy.core.loading.WaitForLoadingPluginActivity;
import com.limpoxe.fairy.core.localservice.LocalServiceManager;
import com.limpoxe.fairy.core.proxy.systemservice.AndroidWebkitWebViewFactoryProvider;
import com.limpoxe.fairy.core.viewfactory.PluginViewFactory;
import com.limpoxe.fairy.manager.PluginActivityMonitor;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginManagerProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.PackageVerifyer;
import com.limpoxe.fairy.util.ProcessUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.limpoxe.fairy.core.PluginLauncher.instance;

/**
 * 插件Activity免注册的主要实现原理。 如有必要，可以增加被代理的方法数量。
 * 
 * @author cailiming
 * 
 */
public class PluginInstrumentionWrapper extends Instrumentation {

	private static final String RELAUNCH_FLAG = "relaunch.category.";

	private final HackInstrumentation hackInstrumentation;
	private Instrumentation real;
	private PluginActivityMonitor monitor;

	public PluginInstrumentionWrapper(Instrumentation instrumentation) {
		this.hackInstrumentation = new HackInstrumentation(instrumentation);
		this.real = instrumentation;
		this.monitor = new PluginActivityMonitor();
	}

	/**
	 * 此方法在application的attach之后被ActivityThread调用
	 * @param app
     */
	@Override
	public void callApplicationOnCreate(Application app) {
		//FIXME 对TabActivity的支持在9上有bug会导致插件Application会被创建两次

		//ContentProvider的相关操作应该放在installContentProvider之后执行,
		//而installContentProvider是ActivityThread在调用application的attach之后,onCreate之前执行
		//因此可以触发ContentProvider调用的最早时机就是这里了
		//下面这个函数内就会触发PluginManagerProvider的调用
		beforeHostCallApplicationOnCreate();
		real.callApplicationOnCreate(app);
	}

	private static void beforeHostCallApplicationOnCreate() {
		LocalServiceManager.init();
		boolean isHostVerionChanged = isHostVerionChanged();
		ArrayList<PluginDescriptor> pluginDescriptorList =  PluginManagerHelper.getPlugins();
		//边循环边删除没问题，因为是不同的列表对象
		for(int i = 0; i < pluginDescriptorList.size(); i++) {
			PluginDescriptor pluginDescriptor = pluginDescriptorList.get(i);
			if (isHostVerionChanged && !PackageVerifyer.isCompatibleWithHost(pluginDescriptor)) {
				LogUtil.e("插件RequiredHostVersionName:" + pluginDescriptor.getRequiredHostVersionName());
				LogUtil.e("当前宿主版本不支持此插件版本，卸载此插件 " + pluginDescriptor.getPackageName());
				PluginManagerHelper.remove(pluginDescriptor.getPackageName());
				LogUtil.e("卸载完成");
			} else if (pluginDescriptor.isBroken()) {
				LogUtil.e("插件文件可能已损坏，卸载此插件 " + pluginDescriptor.getPackageName());
				PluginManagerHelper.remove(pluginDescriptor.getPackageName());
				LogUtil.e("卸载完成");
			} else {
				if (ProcessUtil.isPluginProcess()) {
					LogUtil.v("注册插件内定义的localService");
					LocalServiceManager.registerService(pluginDescriptor);
					LogUtil.v("注册完成");
				}
				if (pluginDescriptor.getAutoStart()) {
					//宿主启动时自动唤醒自启动插件
					LogUtil.w("插件配置了自启动, 唤起插件：" + pluginDescriptor.getPackageName());
					PluginManagerHelper.wakeup(pluginDescriptor.getPackageName());
				}
			}
		}
	}

	private static boolean isHostVerionChanged() {
		//如果宿主进行了覆盖安装的升级操作，移除已经安装的对宿主版本有要求的非独立插件
		String KEY = "last_host_versionName";
		SharedPreferences prefs = FairyGlobal.getHostApplication().getSharedPreferences("fairy_configs", Context.MODE_PRIVATE);
		String lastHostVersoinName = prefs.getString(KEY, null);
		String hostVersionName = null;
		try {
			PackageManager packageManager = FairyGlobal.getHostApplication().getPackageManager();
			PackageInfo hostPackageInfo = packageManager.getPackageInfo(FairyGlobal.getHostApplication().getPackageName(), PackageManager.GET_META_DATA);
			hostVersionName = hostPackageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			LogUtil.printException("PluginLoader.isHostVerionChanged", e);
		}
		boolean isHostVerionChanged = hostVersionName != null && !hostVersionName.equals(lastHostVersoinName);
		//版本号发生了变化, 保存新的版本号
		if (isHostVerionChanged) {
			prefs.edit().putString(KEY, hostVersionName).apply();
		}
		return isHostVerionChanged;
	}

	@Override
	public boolean onException(Object obj, Throwable throwable) {
		try {
            if (obj instanceof Activity) {
                ((Activity) obj).finish();
            } else if (obj instanceof Service) {
                ((Service) obj).stopSelf();
            }
	} catch (Exception e1) {
		//
	}
		return real.onException(obj, throwable);
	}

	@Override
	public Application newApplication(ClassLoader cl, String className, Context context)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (ProcessUtil.isPluginProcess()) {
			PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
			if (pluginDescriptor != null) {
				return instance().getRunningPlugin(pluginDescriptor.getPackageName()).pluginApplication;
			}
		}
		return real.newApplication(cl, className, context);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		ClassLoader orignalCl = cl;
		String orginalClassName = className;
		String orignalIntent = intent.toString();

		if (ProcessUtil.isPluginProcess()) {
			// 将PluginStubActivity替换成插件中的activity
			if (PluginManagerProviderClient.isStub(className)) {

				String action = intent.getAction();

				LogUtil.d("创建插件Activity", action, className);

				if (action != null && action.contains(PluginIntentResolver.CLASS_SEPARATOR)) {
					String[] targetClassName  = action.split(PluginIntentResolver.CLASS_SEPARATOR);
					String pluginClassName = targetClassName[0];

					PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(pluginClassName);

					if (pluginDescriptor != null) {
						boolean isRunning = PluginManagerHelper.isRunning(pluginDescriptor.getPackageName());
						if (!isRunning) {
							if (FairyGlobal.getMinLoadingTime() > 0 && FairyGlobal.getLoadingResId() != 0) {
								return waitForLoading(pluginDescriptor, pluginClassName);
							} else {
								//这个else是为了处理内嵌在tabactivity中的情况, 需要提前start，否则内嵌tab会被拉出tab单独显示
								PluginLauncher.instance().startPlugin(pluginDescriptor);
							}
						}
					}

					Class clazz = PluginLoader.loadPluginClassByName(pluginDescriptor, pluginClassName);

					if (clazz != null) {
						className = pluginClassName;
						cl = clazz.getClassLoader();

						intent.setExtrasClassLoader(cl);
						if (targetClassName.length >1) {
							//之前为了传递classNae，intent的action被修改过 这里再把Action还原到原始的Action
							intent.setAction(targetClassName[1]);
						} else {
							intent.setAction(null);
						}
						//添加一个标记符
						intent.addCategory(RELAUNCH_FLAG + className);
					} else {

						//找不到class，加个容错处理
						className = RealHostClassLoader.TolerantActivity.class.getName();
						cl = RealHostClassLoader.TolerantActivity.class.getClassLoader();
						//添加一个标记符
						intent.addCategory(RELAUNCH_FLAG + className);

						if (pluginDescriptor != null) {
							LogUtil.e("error, remove " + pluginDescriptor.getPackageName());
							PluginManagerHelper.remove(pluginDescriptor.getPackageName());
						}

						//收集状态
						LogUtil.e("ClassNotFound: pluginDescriptor=" + pluginDescriptor
								+ ", pluginClassName=" + pluginClassName +
								", " + (pluginDescriptor==null?"":(pluginDescriptor.getInstalledPath()
								+ ", " + pluginDescriptor.getInstallationTime()
								+ ", " + pluginDescriptor.getVersion()
								+ ", " + (new File(pluginDescriptor.getInstalledPath()).exists()))));

					}
				} else if (PluginManagerProviderClient.isExact(className, PluginDescriptor.ACTIVITY)) {

					//这个逻辑是为了支持外部app唤起配置了stub_exact的插件Activity
					PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);

					if (pluginDescriptor != null && FairyGlobal.getMinLoadingTime() > 0 && FairyGlobal.getLoadingResId() != 0) {
						boolean isRunning = PluginManagerHelper.isRunning(pluginDescriptor.getPackageName());
						if (!isRunning) {
							return waitForLoading(pluginDescriptor, className);
						}
					}

					Class clazz = PluginLoader.loadPluginClassByName(pluginDescriptor, className);

					if (clazz != null) {
						cl = clazz.getClassLoader();
                        //添加一个标记符
                        intent.addCategory(RELAUNCH_FLAG + className);
					} else {

						//收集状态
						LogUtil.e("ClassNotFound: pluginDescriptor=" + pluginDescriptor
								+ ", pluginClassName=" + className +
								", " + (pluginDescriptor==null?"":(pluginDescriptor.getInstalledPath()
								+ ", " + pluginDescriptor.getInstallationTime()
								+ ", " + pluginDescriptor.getVersion()
								+ ", " + (new File(pluginDescriptor.getInstalledPath()).exists()))));

						//精确匹配却找不着目标，有多种可能，其中一个可能是收到外部发来的组件Intent时，插件还没安装
                        //因此这里强行返回容错的class
                        className = RealHostClassLoader.TolerantActivity.class.getName();
                        cl = RealHostClassLoader.TolerantActivity.class.getClassLoader();
                        //添加一个标记符
                        intent.addCategory(RELAUNCH_FLAG + className);

                    }
				} else {
					//进入这个分支可能是因为activity重启了，比如横竖屏切换，由于上面的分支已经把Action还原到原始到Action了
					//这里只能通过之前添加的标记符来查找className
                    LogUtil.e("check with RELAUNCH_FLAG");
					boolean found = false;
					Set<String> category = intent.getCategories();
					if (category != null) {
						Iterator<String> itr = category.iterator();
						while (itr.hasNext()) {
							String cate = itr.next();

							if (cate.startsWith(RELAUNCH_FLAG)) {
								className = cate.replace(RELAUNCH_FLAG, "");

								PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);

								if (pluginDescriptor != null && FairyGlobal.getMinLoadingTime() > 0 && FairyGlobal.getLoadingResId() != 0) {
									boolean isRunning = PluginManagerHelper.isRunning(pluginDescriptor.getPackageName());
									if (!isRunning) {//理论上这里的isRunning应当是true
										return waitForLoading(pluginDescriptor, className);
									}
								}

								Class clazz = PluginLoader.loadPluginClassByName(pluginDescriptor, className);
                                if (clazz != null) {
                                    cl = clazz.getClassLoader();
                                    found = true;
                                } else {
                                    //这里也需要处理STUB_EXACT匹配但插件尚未安装的情况
                                    if (className.equals(RealHostClassLoader.TolerantActivity.class.getName())) {
                                        cl = RealHostClassLoader.TolerantActivity.class.getClassLoader();
                                        found = true;
                                    }
                                }
                                break;
                            }
						}
					}
					if (!found) {
						throw new ClassNotFoundException("className : " + className + ", intent : " + intent.toString(), new Throwable());
					}
				}
			} else {
				//到这里有2中种情况
				//1、确实是宿主Activity
				//2、是插件Activity，但是上面的if没有识别出来（这种情况目前只发现在ActivityGroup情况下会出现，因为ActivityGroup不会触发resolveActivity方法，导致Intent没有更换）
				//判断上述两种情况可以通过ClassLoader的类型来判断, 判断出来以后补一个resolveActivity方法
				if (cl instanceof PluginClassLoader || cl instanceof RealPluginClassLoader) {
					PluginIntentResolver.resolveActivity(intent);
				} else {
					//Do Nothing
				}
			}
		}

		try {
			return real.newActivity(cl, className, intent);
		} catch (ClassNotFoundException e) {
			//收集状态，便于异常分析
			throw new ClassNotFoundException(
					"  orignalCl : " + orignalCl.toString() +
					", orginalClassName : " + orginalClassName +
					", orignalIntent : " + orignalIntent +
					", currentCl : " + cl.toString() +
					", currentClassName : " + className +
					", currentIntent : " + intent.toString() +
					", process : " + ProcessUtil.isPluginProcess() +
					", isStubActivity : " + PluginManagerProviderClient.isStub(orginalClassName) +
					", isExact : " + PluginManagerProviderClient.isExact(orginalClassName, PluginDescriptor.ACTIVITY), e);
		}
	}

	private Activity waitForLoading(PluginDescriptor pluginDescriptor, String targetClassName) {
		WaitForLoadingPluginActivity waitForLoadingPluginActivity = new WaitForLoadingPluginActivity();
		waitForLoadingPluginActivity.setTargetPlugin(pluginDescriptor, targetClassName);
		return waitForLoadingPluginActivity;
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		if (icicle != null && ProcessUtil.isPluginProcess()) {
			if (icicle.getParcelable("android:support:fragments") != null) {
				if (AnnotationProcessor.getPluginContainer(activity.getClass()) != null) {
					// 加了注解的Activity正在自动恢复且页面包含了Fragment。直接清除fragment，
					// 防止如果被恢复的fragment来自插件时，在某些情况下会使用宿主的classloader加载插件fragment
					// 导致classnotfound问题
					icicle.clear();
					icicle = null;
				}
			}
			//处理androidx的fragment缓存在activity自动恢复时导致的classcast问题
			//androidx.fragment.app.FragmentActivity
			//-->androidx.activity.ComponentActivity
			//---->androidx.savedstate.SavedStateRegistryController
			//------>androidx.savedstate.SavedStateRegistry.SAVED_COMPONENTS_KEY
			if (icicle.getParcelable("androidx.lifecycle.BundlableSavedStateRegistry.key") != null) {
				icicle.clear();
				icicle = null;
			}
		}

		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		PluginInjector.injectActivityContext(activity);

		Intent intent = activity.getIntent();

		if (intent != null) {
			intent.setExtrasClassLoader(activity.getClassLoader());
		}

		if (icicle != null) {
			icicle.setClassLoader(activity.getClassLoader());
		}

		if (ProcessUtil.isPluginProcess()) {

			installPluginViewFactory(activity);

			if (activity instanceof WaitForLoadingPluginActivity) {
				//NOTHING
			} else {
				AndroidWebkitWebViewFactoryProvider.switchWebViewContext(activity);
			}

			if (activity.isChild()) {
				//修正TabActivity中的Activity的ContextImpl的packageName
				Context base = activity.getBaseContext();
				while(base instanceof ContextWrapper) {
					base = ((ContextWrapper)base).getBaseContext();
				}
				if (HackContextImpl.instanceOf(base)) {
					HackContextImpl impl = new HackContextImpl(base);
					String packageName = FairyGlobal.getHostApplication().getPackageName();
					String packageName1 = activity.getPackageName();
					impl.setBasePackageName(packageName);
					impl.setOpPackageName(packageName);
				}
			}
		}

		try {
			real.callActivityOnCreate(activity, icicle);
		} catch (RuntimeException e) {
			throw new RuntimeException(
					" activity : " + activity.getClassLoader() +
					" pluginContainer : " + AnnotationProcessor.getPluginContainer(activity.getClass()) +
					", process : " + ProcessUtil.isPluginProcess(), e);
		}

		monitor.onActivityCreate(activity);

	}

	private void installPluginViewFactory(Activity activity) {
		PluginContainer container = AnnotationProcessor.getPluginContainer(activity.getClass());
		// 如果配置了插件容器注解,安装PluginViewFactory,用于支持<pluginView>
		if (container != null) {
			new PluginViewFactory(activity, activity.getWindow(), new PluginViewCreator()).installViewFactory();
		}
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		monitor.onActivityDestory(activity);

		real.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (savedInstanceState != null) {
			savedInstanceState.setClassLoader(activity.getClassLoader());
			// defined by Activity.java
			final String WINDOW_HIERARCHY_TAG = "android:viewHierarchyState";
			Bundle windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG);
			if (windowState != null) {
				windowState.setClassLoader(activity.getClassLoader());
			}
		}

		real.callActivityOnRestoreInstanceState(activity, savedInstanceState);
	}

	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (icicle != null) {
			icicle.setClassLoader(activity.getClassLoader());
		}

		real.callActivityOnPostCreate(activity, icicle);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (intent != null) {
			intent.setExtrasClassLoader(activity.getClassLoader());
		}

		real.callActivityOnNewIntent(activity, intent);
	}

	@Override
	public void callActivityOnStart(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnStart(activity);
	}

	@Override
	public void callActivityOnRestart(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnRestart(activity);
	}

	@Override
	public void callActivityOnResume(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnResume(activity);

		monitor.onActivityResume(activity);
	}

	@Override
	public void callActivityOnStop(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnStop(activity);
	}

	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (outState != null) {
			outState.setClassLoader(activity.getClassLoader());
		}

		real.callActivityOnSaveInstanceState(activity, outState);
	}

	@Override
	public void callActivityOnPause(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnPause(activity);

		monitor.onActivityPause(activity);
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		real.callActivityOnUserLeaving(activity);
	}

	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivity(who, contextThread, token, target,
				intent, requestCode, options);
	}

	public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent[] intents, Bundle options) {

		PluginIntentResolver.resolveActivity(intents);

		hackInstrumentation.execStartActivities(who, contextThread, token, target, intents, options);
	}

	public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent[] intents, Bundle options, int userId) {

		PluginIntentResolver.resolveActivity(intents);

		hackInstrumentation.execStartActivitiesAsUser(who, contextThread, token, target, intents, options, userId);
	}

	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
			Fragment target, Intent intent, int requestCode, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options, UserHandle user) {

		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode, options, user);
	}


	/////////////  Android 4.0.4及以下  ///////////////

	public ActivityResult execStartActivity(
				Context who, IBinder contextThread, IBinder token, Activity target,
				Intent intent, int requestCode) {

		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode);
	}

	public void execStartActivities(Context who, IBinder contextThread,
														IBinder token, Activity target, Intent[] intents) {
		PluginIntentResolver.resolveActivity(intents);

		hackInstrumentation.execStartActivities(who, contextThread, token, target, intents);
	}

	public ActivityResult execStartActivity(
			Context who, IBinder contextThread, IBinder token, Fragment target,
			Intent intent, int requestCode) {

		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode);
	}

	/////// For Android 5.1
	public ActivityResult execStartActivityAsCaller(
			            Context who, IBinder contextThread, IBinder token, Activity target,
			            Intent intent, int requestCode, Bundle options, int userId) {
		PluginIntentResolver.resolveActivity(intent);

		return hackInstrumentation.execStartActivityAsCaller(who, contextThread, token, target, intent, requestCode, options, userId);
	}

	public void execStartActivityFromAppTask(
			            Context who, IBinder contextThread, Object appTask,
			            Intent intent, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		hackInstrumentation.execStartActivityFromAppTask(who, contextThread, appTask, intent, options);
	}

	//7.1?
    public ActivityResult execStartActivityAsCaller(Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, boolean ignoreTargetSecurity,
            int userId) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivityAsCaller(who, contextThread, token, target, intent, requestCode, options, ignoreTargetSecurity, userId);
    }

}
