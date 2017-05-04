package com.limpoxe.fairy.core;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.limpoxe.fairy.core.android.HackContextImpl;
import com.limpoxe.fairy.core.android.HackService;
import com.limpoxe.fairy.util.LogUtil;

/**
 * 此类用于修正service的中的context
 */
public class PluginShadowService extends Service {

	public Context mBaseContext = null;
	public Object mThread = null;
	public String mClassName = null;
	public IBinder mToken = null;
	public Application mApplication = null;
	public Object mActivityManager = null;
	public Boolean mStartCompatibility = false;

	public Service realService;

	@Override
	public void onCreate() {
		super.onCreate();

		getAttachParam();

		callServiceOnCreate();
	}

	private void getAttachParam() {
		mBaseContext = getBaseContext();
		HackService hackService = new HackService(this);
		mThread = hackService.getThread();
		mClassName = hackService.getClassName();
		mToken = hackService.getToken();
		mApplication = getApplication();
		mActivityManager = hackService.getActivityManager();
		mStartCompatibility = hackService.getStartCompatibility();
	}

	private void callServiceOnCreate() {
		String realName = mClassName;
		try {
			realName = mClassName.replace(PluginIntentResolver.CLASS_PREFIX_SERVICE, "");
			LogUtil.v("className ", mClassName, "target", realName);
			Class clazz = PluginLoader.loadPluginClassByName(realName);
			realService = (Service) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to instantiate service " + mClassName
							+ ", realName " + realName + " : " + e.toString(), e);
		}

		try {
			new HackContextImpl(mBaseContext).setOuterContext(realService);
			HackService hackService = new HackService(realService);
			hackService.attach(mBaseContext, mThread, mClassName, mToken, mApplication, mActivityManager);
			hackService.setStartCompatibility(mStartCompatibility);

			//拿到创建好的service，重新 设置mBase和mApplicaiton
			PluginInjector.replacePluginServiceContext(realName, realService);

			realService.onCreate();
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to create service " + mClassName
							+ ": " + e.toString(), e);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
