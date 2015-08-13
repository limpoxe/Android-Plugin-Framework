package com.plugin.core;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Service;
import android.app.Instrumentation.ActivityMonitor;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import com.plugin.core.ui.stub.PluginStubActivity;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

 /**
  * 插件Activity免注册的主要实现原理。
  * 如有必要，可以增加被代理的方法数量。
  * @author cailiming
  *
  */
public class PluginInstrumentionWrapper extends Instrumentation {
	
	private static final String ACTIVITY_NAME_IN_PLUGIN = "InstrumentationWrapper.className";
	
	private Instrumentation realInstrumention;
	
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
		return realInstrumention.onException(obj, e);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		//将PluginStubActivity替换成插件中的activity
		if (className.equals(PluginStubActivity.class.getName())) {
			LogUtil.d("className", className, intent.toUri(0));
			String targetClassName = intent.getStringExtra(ACTIVITY_NAME_IN_PLUGIN);	
			if (targetClassName != null) {
				@SuppressWarnings("rawtypes")
				Class clazz = PluginLoader.loadPluginClassByName(targetClassName);
				if (clazz != null) {
					return (Activity)clazz.newInstance();
				}
			}
		}

		return realInstrumention.newActivity(cl, className, intent);
	}

	private static void checkInstrumetionFor360Safe(Activity activity) {
		//检查mInstrumention是否已经替换成功。
		//之所以要检查，是因为如果手机上安装了360手机卫士等app，它们可能会劫持用户app的ActivityThread对象，
		//导致在PluginApplication的onCreate方法里面替换mInstrumention可能会失败
		//所以这里再做一次检查
		Instrumentation instrumention = (Instrumentation)RefInvoker.getFieldObject(activity, Activity.class.getName(), "mInstrumentation");
		if (!(instrumention instanceof PluginInstrumentionWrapper)) {
			//说明被360还原了，这里再次尝试替换
			RefInvoker.setFieldObject(activity, Activity.class.getName(), "mInstrumentation",
					new PluginInstrumentionWrapper(instrumention));
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		
		checkInstrumetionFor360Safe(activity);
		
		Intent intent = activity.getIntent();
		if (intent.getComponent() != null) {
			if (intent.getComponent().getClassName().equals(PluginStubActivity.class.getName())) {
				//为了不需要重写插件Activity的attachBaseContext方法为：
				//@Override
				//protected void attachBaseContext(Context newBase) {
				//	super.attachBaseContext(PluginLoader.getDefaultPluginContext(PluginNotInManifestActivity.class));
				//}
				//我们在activityoncreate之前去完成attachBaseContext的事情
				
				//重设BaseContext
				LogUtil.d("mBase attachBaseContext", activity.getClass().getName());
				Context pluginContext = PluginLoader.getDefaultPluginContext(activity.getClass());
				RefInvoker.setFieldObject(activity, ContextWrapper.class.getName(), "mBase", null);
				RefInvoker.invokeMethod(activity, ContextThemeWrapper.class.getName(), "attachBaseContext", 
						new Class[]{Context.class}, 
						new Object[]{pluginContext});

				//重设LayoutInflater
				LogUtil.d(activity.getWindow().getClass().getName());
				RefInvoker.setFieldObject(activity.getWindow(), activity.getWindow().getClass().getName(), 
						"mLayoutInflater", LayoutInflater.from(pluginContext));
				
				//如果api>=11,还要重设factory2
				if (Build.VERSION.SDK_INT >=11) {
					RefInvoker.invokeMethod(activity.getWindow().getLayoutInflater(), LayoutInflater.class.getName(), "setPrivateFactory", 
							new Class[]{LayoutInflater.Factory2.class}, 
							new Object[]{activity});
				}
				
				//由于在attach的时候Resource已经被初始化了，所以还需要重置Resource
				RefInvoker.setFieldObject(activity, ContextThemeWrapper.class.getName(), "mResources", null);
                
				//重设theme
				ActivityInfo activityInfo = (ActivityInfo)RefInvoker.getFieldObject(activity, Activity.class.getName(), "mActivityInfo");
				int theme = activityInfo.getThemeResource();
                if (theme != 0) {
                	RefInvoker.setFieldObject(activity, ContextThemeWrapper.class.getName(), "mTheme", null);
                    activity.setTheme(theme);
                }
			}
		}
		
		realInstrumention.callActivityOnCreate(activity, icicle);
	}
	
	@Override
    public void callActivityOnDestroy(Activity activity) {
		checkInstrumetionFor360Safe(activity);
		super.callActivityOnDestroy(activity);
    }

	@Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
		checkInstrumetionFor360Safe(activity);
    	super.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

	@Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
		checkInstrumetionFor360Safe(activity);
    	super.callActivityOnPostCreate(activity, icicle);
    }

	@Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
		checkInstrumetionFor360Safe(activity);
        super.callActivityOnNewIntent(activity, intent);
    }

	@Override
    public void callActivityOnStart(Activity activity) {
		checkInstrumetionFor360Safe(activity);
        super.callActivityOnStart(activity);
    }

	@Override
    public void callActivityOnRestart(Activity activity) {
		checkInstrumetionFor360Safe(activity);
        super.callActivityOnRestart(activity);
    }

	@Override
    public void callActivityOnResume(Activity activity) {
		checkInstrumetionFor360Safe(activity);
        super.callActivityOnResume(activity);
    }

	@Override
    public void callActivityOnStop(Activity activity) {
		checkInstrumetionFor360Safe(activity);
    	super.callActivityOnStop(activity);
    }

	@Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
		checkInstrumetionFor360Safe(activity);
        super.callActivityOnSaveInstanceState(activity, outState);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
    	checkInstrumetionFor360Safe(activity);
        super.callActivityOnPause(activity);
    }
    
    @Override
    public void callActivityOnUserLeaving(Activity activity) {
    	checkInstrumetionFor360Safe(activity);
        super.callActivityOnUserLeaving(activity);
    }
	
	public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
		
		resloveIntent(intent);
		
		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
				"execStartActivity", new Class[]{
            Context.class, IBinder.class, IBinder.class, Activity.class,
            Intent.class, int.class, Bundle.class}, new Object[]{
             who, contextThread, token, target,
            intent, requestCode, options});
		
		return (ActivityResult)result;
	}

    public void execStartActivities(Context who, IBinder contextThread,
            IBinder token, Activity target, Intent[] intents, Bundle options) {
    	
    	resloveIntent(intents);
    	
    	RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
				"execStartActivities", new Class[]{
            Context.class, IBinder.class, IBinder.class, Activity.class,
            Intent[].class, Bundle.class}, new Object[]{
             who, contextThread, token, target,
            intents, options});
    }

    public void execStartActivitiesAsUser(Context who, IBinder contextThread,
            IBinder token, Activity target, Intent[] intents, Bundle options,
            int userId) {
    	
    	resloveIntent(intents);
    	
    	RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
				"execStartActivitiesAsUser", new Class[]{
            Context.class, IBinder.class, IBinder.class, Activity.class,
            Intent[].class, Bundle.class, int.class}, new Object[]{
             who, contextThread, token, target,
            intents, options, userId});
    }

    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, android.support.v4.app.Fragment target,
        Intent intent, int requestCode, Bundle options) {

    	resloveIntent(intent);
    	
		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
				"execStartActivity", new Class[]{
            Context.class, IBinder.class, IBinder.class, android.support.v4.app.Fragment.class,
            Intent.class, int.class, Bundle.class}, new Object[]{
             who, contextThread, token, target,
            intent, requestCode, options});
		
		return (ActivityResult)result;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, android.app.Fragment target,
            Intent intent, int requestCode, Bundle options) {

    		resloveIntent(intent);
    	
    		Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
    				"execStartActivity", new Class[]{
                Context.class, IBinder.class, IBinder.class, android.app.Fragment.class,
                Intent.class, int.class, Bundle.class}, new Object[]{
                 who, contextThread, token, target,
                intent, requestCode, options});
    		
    		return (ActivityResult)result;
        }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
    	
    	resloveIntent(intent);
    	
    	Object result = RefInvoker.invokeMethod(realInstrumention, android.app.Instrumentation.class.getName(), 
				"execStartActivity", new Class[]{
            Context.class, IBinder.class, IBinder.class, Activity.class,
            Intent.class, int.class, Bundle.class, UserHandle.class}, new Object[]{
             who, contextThread, token, target,
            intent, requestCode, options, user});
		
		return (ActivityResult)result;
    }
    
    /*package*/ static void resloveIntent(Intent intent) {
    	//如果在插件中发现Intent的匹配项，记下匹配的插件Activity的ClassName
    	String className = PluginLoader.isMatchPlugin(intent);
    	if (className != null) {
    		intent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(), PluginStubActivity.class.getName()));
    		intent.putExtra(ACTIVITY_NAME_IN_PLUGIN, className);
    	}
    }
    
    private static void resloveIntent(Intent[] intent) {
    	//not needed
    }

}
