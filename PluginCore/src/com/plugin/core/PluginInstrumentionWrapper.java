package com.plugin.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

import com.plugin.core.ui.stub.PluginStubActivity;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.util.Iterator;
import java.util.Set;

/**
 * 插件Activity免注册的主要实现原理。 如有必要，可以增加被代理的方法数量。
 * 
 * @author cailiming
 * 
 */
public class PluginInstrumentionWrapper extends Instrumentation {

	private static final String RELAUNCH_FLAG = "relaunch.category.";

	private final Instrumentation realInstrumention;

	public PluginInstrumentionWrapper(Instrumentation instrumentation) {
		this.realInstrumention = instrumentation;
	}

	@Override
	public boolean onException(Object obj, Throwable e) {
		if (obj instanceof Activity) {
			((Activity) obj).finish();
		} else if (obj instanceof Service) {
			((Service) obj).stopSelf();
		}
		return super.onException(obj, e);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// 将PluginStubActivity替换成插件中的activity
		if (className.equals(PluginStubActivity.class.getName())) {

			String action = intent.getAction();

			//
			if (action != null && action.contains(PluginIntentResolver.ACTIVITY_ACTION_IN_PLUGIN)) {

				String[] targetClassName  = action.split(PluginIntentResolver.ACTIVITY_ACTION_IN_PLUGIN);

				LogUtil.d(className, action, targetClassName[0]);

				className = targetClassName[0];
				Class clazz = PluginLoader.loadPluginClassByName(className);
				cl = clazz.getClassLoader();

				intent.setExtrasClassLoader(cl);

				//之前为了传递classNae，intent的action被修改过 这里再把Action还原到原始的Action
				if (targetClassName.length >1) {
					intent.setAction(targetClassName[1]);
				} else {
					intent.setAction(null);
				}
				//添加一个标记符
				intent.addCategory(RELAUNCH_FLAG + className);

			} else {
				//进入这个分支可能是因为activity重启了，比如横竖屏切换，由于上面的分支已经把Action还原到原始到Action了
				//这里只能通过之前添加的标记符来查找className
				Set<String> category = intent.getCategories();
				if (category != null) {
					Iterator<String> itr = category.iterator();
					while (itr.hasNext()) {
						String cate = itr.next();

						if (cate.startsWith(RELAUNCH_FLAG)) {
							className = cate.replace(RELAUNCH_FLAG, "");

							Class clazz = PluginLoader.loadPluginClassByName(className);
							cl = clazz.getClassLoader();
							break;
						}
					}
				}
			}
		}

		return super.newActivity(cl, className, intent);
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {

		PluginInjector.injectInstrumetionFor360Safe(activity, this);

		PluginInjector.injectActivityContext(activity);

		Intent intent = activity.getIntent();

		if (intent != null) {
			intent.setExtrasClassLoader(activity.getClassLoader());
		}

		super.callActivityOnCreate(activity, icicle);
	}


	@Override
	public void callActivityOnDestroy(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
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
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {
		PluginInjector.injectInstrumetionFor360Safe(activity, this);
		super.callActivityOnUserLeaving(activity);
	}

	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivity", new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
						Intent.class, int.class, Bundle.class }, new Object[] { who, contextThread, token, target,
						intent, requestCode, options });

		return (ActivityResult) result;
	}

	public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent[] intents, Bundle options) {

		PluginIntentResolver.resolveActivity(intents);

		RefInvoker
				.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), "execStartActivities",
						new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent[].class,
								Bundle.class}, new Object[]{who, contextThread, token, target, intents, options});
	}

	public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent[] intents, Bundle options, int userId) {

		PluginIntentResolver.resolveActivity(intents);

		RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivitiesAsUser", new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
						Intent[].class, Bundle.class, int.class }, new Object[] { who, contextThread, token, target,
						intents, options, userId });
	}

	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
			Fragment target, Intent intent, int requestCode, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivity", new Class[] { Context.class, IBinder.class, IBinder.class,
						Fragment.class, Intent.class, int.class, Bundle.class }, new Object[] { who,
						contextThread, token, target, intent, requestCode, options });

		return (ActivityResult) result;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options, UserHandle user) {

		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivity", new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
						Intent.class, int.class, Bundle.class, UserHandle.class }, new Object[] { who, contextThread,
						token, target, intent, requestCode, options, user });

		return (ActivityResult) result;
	}


	/////////////  Android 4.0.4及以下  ///////////////

	public ActivityResult execStartActivity(
				Context who, IBinder contextThread, IBinder token, Activity target,
				Intent intent, int requestCode) {

		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivity", new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
						Intent.class, int.class }, new Object[] { who, contextThread,
						token, target, intent, requestCode });

		return (ActivityResult) result;
	}

	public void execStartActivities(Context who, IBinder contextThread,
														IBinder token, Activity target, Intent[] intents) {
		PluginIntentResolver.resolveActivity(intents);

		RefInvoker
				.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), "execStartActivities",
						new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent[].class},
						new Object[]{who, contextThread, token, target, intents});
	}

	public ActivityResult execStartActivity(
			Context who, IBinder contextThread, IBinder token, Fragment target,
			Intent intent, int requestCode) {

		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivity", new Class[] { Context.class, IBinder.class, IBinder.class, Fragment.class,
						Intent.class, int.class }, new Object[] { who, contextThread,
						token, target, intent, requestCode });

		return (ActivityResult) result;
	}

	/////// For Android 5.1
	public ActivityResult execStartActivityAsCaller(
			            Context who, IBinder contextThread, IBinder token, Activity target,
			            Intent intent, int requestCode, Bundle options, int userId) {
		PluginIntentResolver.resolveActivity(intent);

		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(),
				"execStartActivityAsCaller", new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
						Intent.class, int.class, Bundle.class, int.class}, new Object[] { who, contextThread,
						token, target, intent, requestCode, options, userId});
		return (ActivityResult)result;
	}

	public void execStartActivityFromAppTask(
			            Context who, IBinder contextThread, Object appTask,
			            Intent intent, Bundle options) {

		PluginIntentResolver.resolveActivity(intent);

		try {
			RefInvoker.invokeMethod(realInstrumention, Instrumentation.class.getName(),
					"execStartActivityFromAppTask", new Class[]{Context.class, IBinder.class,
							Class.forName("android.app.IAppTask"), Intent.class, Bundle.class,},
					new Object[]{who, contextThread, appTask, intent, options});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
