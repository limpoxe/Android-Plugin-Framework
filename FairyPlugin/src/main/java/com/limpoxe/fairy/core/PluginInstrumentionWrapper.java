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
import com.limpoxe.fairy.manager.PluginProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

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
	private PluginActivityMonitor monitor;

	public PluginInstrumentionWrapper(Instrumentation instrumentation) {
		this.hackInstrumentation = new HackInstrumentation(instrumentation);
		this.monitor = new PluginActivityMonitor();
	}

	/**
	 *
	 * @param app
     */
	@Override
	public void callApplicationOnCreate(Application app) {
		//此方法在application的attach之后被ActivityThread调用
		super.callApplicationOnCreate(app);

		//ContentProvider的相关操作应该放在installContentProvider之后执行,
		//而installContentProvider是ActivityThread在调用application的attach之后,onCreate之前执行
		// 因此下面的初始化操作的最佳时机是在application的oncreate之前执行
		LocalServiceManager.init();
		if (ProcessUtil.isPluginProcess()) {
			Iterator<PluginDescriptor> itr = PluginManagerHelper.getPlugins().iterator();
			while (itr.hasNext()) {
				PluginDescriptor plugin = itr.next();
				LocalServiceManager.registerService(plugin);
			}
		}
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
		return super.onException(obj, throwable);
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
		return super.newApplication(cl, className, context);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		ClassLoader orignalCl = cl;
		String orginalClassName = className;
		String orignalIntent = intent.toString();

		if (ProcessUtil.isPluginProcess()) {
			// 将PluginStubActivity替换成插件中的activity
			if (PluginProviderClient.isStub(className)) {

				String action = intent.getAction();

				LogUtil.d("创建插件Activity", action, className);

				if (action != null && action.contains(PluginIntentResolver.CLASS_SEPARATOR)) {
					String[] targetClassName  = action.split(PluginIntentResolver.CLASS_SEPARATOR);
					String pluginClassName = targetClassName[0];

					PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(pluginClassName);

					if (pluginDescriptor != null) {
						boolean isRunning = PluginLauncher.instance().isRunning(pluginDescriptor.getPackageName());
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
						throw new ClassNotFoundException("pluginClassName : " + pluginClassName, new Throwable());
					}
				} else if (PluginProviderClient.isExact(className, PluginDescriptor.ACTIVITY)) {

					//这个逻辑是为了支持外部app唤起配置了stub_exact的插件Activity
					PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);

					if (pluginDescriptor != null && FairyGlobal.getMinLoadingTime() > 0 && FairyGlobal.getLoadingResId() != 0) {
						boolean isRunning = PluginLauncher.instance().isRunning(pluginDescriptor.getPackageName());
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
						//精确匹配却找不着目标，有多种可能，其中一个可能是收到外部发来的组件Intent时，插件还没安装
                        //因此这里强行返回容错的class
                        className = HostClassLoader.TolerantActivity.class.getName();
                        cl = HostClassLoader.TolerantActivity.class.getClassLoader();
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
									boolean isRunning = PluginLauncher.instance().isRunning(pluginDescriptor.getPackageName());
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
                                    if (className.equals(HostClassLoader.TolerantActivity.class.getName())) {
                                        cl = HostClassLoader.TolerantActivity.class.getClassLoader();
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
				if (cl instanceof PluginClassLoader) {
					PluginIntentResolver.resolveActivity(intent);
				} else {
					//Do Nothing
				}
			}
		}

		try {
			return super.newActivity(cl, className, intent);
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
					", isStubActivity : " + PluginProviderClient.isStub(orginalClassName) +
					", isExact : " + PluginProviderClient.isExact(orginalClassName, PluginDescriptor.ACTIVITY), e);
		}
	}

	private Activity waitForLoading(PluginDescriptor pluginDescriptor, String targetClassName) {
		WaitForLoadingPluginActivity waitForLoadingPluginActivity = new WaitForLoadingPluginActivity();
		waitForLoadingPluginActivity.setTargetPlugin(pluginDescriptor, targetClassName);
		return waitForLoadingPluginActivity;
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {

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

		super.callActivityOnCreate(activity, icicle);

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

		super.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (savedInstanceState != null) {
			savedInstanceState.setClassLoader(activity.getClassLoader());
		}

		super.callActivityOnRestoreInstanceState(activity, savedInstanceState);
	}

	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (icicle != null) {
			icicle.setClassLoader(activity.getClassLoader());
		}

		super.callActivityOnPostCreate(activity, icicle);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (intent != null) {
			intent.setExtrasClassLoader(activity.getClassLoader());
		}

		super.callActivityOnNewIntent(activity, intent);
	}

	@Override
	public void callActivityOnStart(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnStart(activity);
	}

	@Override
	public void callActivityOnRestart(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnRestart(activity);
	}

	@Override
	public void callActivityOnResume(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnResume(activity);

		monitor.onActivityResume(activity);
	}

	@Override
	public void callActivityOnStop(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnStop(activity);
	}

	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		if (outState != null) {
			outState.setClassLoader(activity.getClassLoader());
		}

		super.callActivityOnSaveInstanceState(activity, outState);
	}

	@Override
	public void callActivityOnPause(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnPause(activity);

		monitor.onActivityPause(activity);
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnUserLeaving(activity);
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
