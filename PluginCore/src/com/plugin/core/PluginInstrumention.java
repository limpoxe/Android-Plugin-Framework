package com.plugin.core;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.plugin.core.ui.PluginStubActivity;

public class PluginInstrumention extends Instrumentation {
	private static final String LOG_TAG = "PluginInstrumention";
	
	@Override
	public boolean onException(Object obj, Throwable e) {
		if (obj instanceof Activity) {
			((Activity) obj).finish();
		} else if (obj instanceof Service) {
			((Service) obj).stopSelf();
		}
		return false;
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		
		String targetClassName = intent.getStringExtra("className");
		String targetId = intent.getStringExtra("classId");
		
		Log.d(LOG_TAG, intent.toUri(0));
		Log.d(LOG_TAG, "className = " + className + ", targetClassName = " + targetClassName + ", targetId = " + targetId);
		
		if (className.equals(PluginStubActivity.class.getName())) {
			
			if (targetClassName != null) {
				@SuppressWarnings("rawtypes")
				Class clazz = PluginLoader.loadPluginClassByName(targetClassName);
				if (clazz != null) {
					return (Activity)clazz.newInstance();
				}
			} else if (targetId != null) {
				@SuppressWarnings("rawtypes")
				Class clazz = PluginLoader.loadPluginClassById(targetId);
				if (clazz != null) {
					return (Activity)clazz.newInstance();
				}
			}
		}
	
		return super.newActivity(cl, className, intent);
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		//attach base context 
		//set theme
		super.callActivityOnCreate(activity, icicle);
	}

}
