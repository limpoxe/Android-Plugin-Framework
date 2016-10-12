package com.limpoxe.fairy.core;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.limpoxe.fairy.core.app.ActivityThread;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

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
		mThread = RefInvoker.getFieldObject(this, Service.class, "mThread");
		mClassName = (String)RefInvoker.getFieldObject(this, Service.class, "mClassName");
		mToken = (IBinder) RefInvoker.getFieldObject(this, Service.class, "mToken"); ;
		mApplication = getApplication();
		mActivityManager = RefInvoker.getFieldObject(this, Service.class, "mActivityManager");
		mStartCompatibility = (Boolean)RefInvoker.getFieldObject(this, Service.class, "mStartCompatibility");
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
							+ ": " + e.toString(), e);
		}

		try {
			RefInvoker.invokeMethod(mBaseContext, "android.app.ContextImpl", "setOuterContext",
					new Class[]{Context.class}, new Object[]{realService});
			RefInvoker.invokeMethod(realService, Service.class, "attach",
					new Class[]{Context.class,
							ActivityThread.clazz(), String.class, IBinder.class,
							Application.class, Object.class},
					new Object[]{mBaseContext, mThread, mClassName, mToken,
							mApplication, mActivityManager});
			RefInvoker.setFieldObject(realService, Service.class, "mStartCompatibility", mStartCompatibility);

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
