package com.plugin.core;

import com.plugin.core.ui.PluginStubActivity;
import com.plugin.util.RefInvoker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.ContextThemeWrapper;

 /**
  * 如有必要，可以增加被代理的方法数量
  * @author cailiming
  *
  */
public class PluginInstrumentionWrapper extends Instrumentation {
	private static final String LOG_TAG = "PluginInstrumention";
	
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
		
		String targetClassName = intent.getStringExtra("className");

		Log.d(LOG_TAG, "className = " + className + ", " + intent.toUri(0));
		
		if (className.equals(PluginStubActivity.class.getName())) {
			
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

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		
		//先判断如果是插件Activity
		Intent intent = activity.getIntent();
		if (false && intent.getComponent() != null) {
			if (intent.getComponent().getClassName().equals(PluginStubActivity.class.getName())) {
				
				//attach base context已经被系统调用过了，所以这里需要通过反射的方式重置为插件Context
				Log.d(LOG_TAG,"attach base context " + activity.getClass().getName());
				Context pluginContext = PluginLoader.getDefaultPluginContext(activity.getClass());
				RefInvoker.setFieldObject(activity, ContextWrapper.class.getName(), "mBase", null);
				RefInvoker.invokeMethod(activity, ContextThemeWrapper.class.getName(), "attachBaseContext", 
						new Class[]{Context.class}, 
						new Object[]{pluginContext});

				ActivityInfo activityInfo = (ActivityInfo)RefInvoker.getFieldObject(activity, Activity.class.getName(), "mActivityInfo");
				//重新设置theme
				int theme = activityInfo.getThemeResource();
                if (theme != 0) {
                	RefInvoker.setFieldObject(activity, ContextThemeWrapper.class.getName(), "mTheme", null);
                    activity.setTheme(theme);
                }
			}
		}
		
		realInstrumention.callActivityOnCreate(activity, icicle);
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
    
    public static void resloveIntent(Intent intent) {
    	String className = PluginLoader.isMatchPlugin(intent);
    	if (className != null) {
    		Log.d("InstrumentationWrapper", "resloveIntent Before:" + intent.toUri(0));
    		intent.putExtra("className", className);
    		intent.setComponent(new ComponentName(PluginLoader.getApplicatoin().getPackageName(), PluginStubActivity.class.getName()));
    		Log.d("InstrumentationWrapper", "resloveIntent After:" + intent.toUri(0));
    	}
    }
    
    private static void resloveIntent(Intent[] intent) {
    	//TODO
    }

}
