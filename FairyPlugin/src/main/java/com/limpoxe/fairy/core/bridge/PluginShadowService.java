package com.limpoxe.fairy.core.bridge;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.limpoxe.fairy.core.PluginInjector;
import com.limpoxe.fairy.core.PluginIntentResolver;
import com.limpoxe.fairy.core.PluginLoader;
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

	public PluginShadowService() {
		LogUtil.d("PluginShadowService()");
	}

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
			LogUtil.e("callServiceOnCreate", "Unable to instantiate service " + mClassName
					+ ", realName " + realName + " : " + e.toString());
		}

		if(realService == null) {
			return;
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
			LogUtil.e("callServiceOnCreate", "Unable to create service " + mClassName
					+ ": " + e.toString());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.d("onBind", "PluginShadowService -> " + mClassName);
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// AndroidAppIActivityManager.serviceDoneExecuting会将realService替换ApplicationThread中的mServices的内容
		// 如果PluginShadowService的onStartCommand被触发则说明realService替换失败了
		LogUtil.e("onStartCommand", "PluginShadowService should not call onStartCommand! -> " + mClassName);
		return super.onStartCommand(intent, flags, startId);
	}
}
