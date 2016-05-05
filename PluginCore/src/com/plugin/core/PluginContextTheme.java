package com.plugin.core;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.core.multidex.PluginMultiDexHelper;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.io.File;
import java.util.ArrayList;

public class PluginContextTheme extends PluginBaseContextWrapper {
	private int mThemeResource;
	Resources.Theme mTheme;
	private LayoutInflater mInflater;

	Resources mResources;
	private final ClassLoader mClassLoader;
	private Application mPluginApplication;
	protected final PluginDescriptor mPluginDescriptor;

	private ArrayList<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();

	private boolean crackPackageManager = false;

	public PluginContextTheme(PluginDescriptor pluginDescriptor,
							  Context base, Resources resources,
							  ClassLoader classLoader) {
		super(base);
		mPluginDescriptor = pluginDescriptor;
		mResources = resources;
		mClassLoader = classLoader;
	}

	public void setPluginApplication(Application pluginApplication) {
		this.mPluginApplication = pluginApplication;
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
	}

	@Override
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}

	@Override
	public AssetManager getAssets() {
		return mResources.getAssets();
	}

	@Override
	public Resources getResources() {
		return mResources;
	}

	/**
	 * 传0表示使用系统默认主题，最终的现实样式和客户端程序的minSdk应该有关系。 即系统针对不同的minSdk设置了不同的默认主题样式
	 * 传非0的话表示传过来什么主题就显示什么主题
	 */
	@Override
	public void setTheme(int resid) {
		mThemeResource = resid;
		initializeTheme();
	}

	@Override
	public Resources.Theme getTheme() {
		if (mTheme != null) {
			return mTheme;
		}

		Object result = RefInvoker.invokeStaticMethod(Resources.class.getName(), "selectDefaultTheme", new Class[]{
				int.class, int.class}, new Object[]{mThemeResource,
				getBaseContext().getApplicationInfo().targetSdkVersion});
		if (result != null) {
			mThemeResource = (Integer) result;
		}

		initializeTheme();

		return mTheme;
	}

	@Override
	public Object getSystemService(String name) {
		if (LAYOUT_INFLATER_SERVICE.equals(name)) {
			if (mInflater == null) {
				mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
			}
			return mInflater;
		}

		Object service = getBaseContext().getSystemService(name);

		if (service == null) {
			service = LocalServiceManager.getService(name);
		}

		return service;
	}


	private void initializeTheme() {
		final boolean first = mTheme == null;
		if (first) {
			mTheme = getResources().newTheme();
			Resources.Theme theme = getBaseContext().getTheme();
			if (theme != null) {
				mTheme.setTo(theme);
			}
		}
		mTheme.applyStyle(mThemeResource, true);
	}

	@Override
	public String getPackageName() {
		//如果返回插件本身的packageName可能会引起一些问题。
		//如packagemanager、activitymanager、wifi、window、inputservice
		//等等系统服务会获取packageName去查询信息，如果获取到插件的packageName则会crash
		//除非再增加对系统服务方法hook才能解决
		//最简单的办法还是这里保留返回宿主的packageName，
		//在代码中自行区分是需要使用插件自己的还是宿主的
		//if (mPluginDescriptor.isStandalone()) {
		//	return mPluginDescriptor.getPackageName();
		//} else {
			return super.getPackageName();
		//}
	}

	/**
	 * 隔离插件间的SharedPreferences
	 * @param name
	 * @param mode
	 * @return
	 */
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		String realName = mPluginDescriptor.getPackageName() + "_" + name;
		LogUtil.d(realName);
		return super.getSharedPreferences(realName, mode);
	}

	/**
	 * 隔离插件间的Database
	 * @param name
	 * @param mode
	 * @param factory
	 * @return
	 */
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
		String realName = mPluginDescriptor.getPackageName() + "_" + name;
		LogUtil.d(realName);
		return super.openOrCreateDatabase(realName, mode, factory);
	}

	/**
	 * 隔离插件间的Database
	 * @param name
	 * @param mode
	 * @param factory
	 * @return
	 */
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
		String realName = mPluginDescriptor.getPackageName() + "_" + name;
		LogUtil.d(realName);
		return super.openOrCreateDatabase(realName, mode, factory, errorHandler);
	}

	@Override
	public boolean deleteDatabase(String name) {
		String realName = mPluginDescriptor.getPackageName() + "_" + name;
		LogUtil.d(realName);
		return super.deleteDatabase(realName);
	}

	@Override
	public File getDatabasePath(String name) {
		String realName = mPluginDescriptor.getPackageName() + "_" + name;
		LogUtil.d(realName);
		return super.getDatabasePath(realName);
	}

	@Override
	public Context getApplicationContext() {
		return mPluginApplication;
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return super.getApplicationInfo();
	}

	@Override
	public String getPackageCodePath() {
		return mPluginDescriptor.getInstalledPath();
	}

	@Override
	public String getPackageResourcePath() {
		return mPluginDescriptor.getInstalledPath();
	}

	public PluginDescriptor getPluginDescriptor() {
		return mPluginDescriptor;
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		receivers.add(receiver);
		return super.registerReceiver(receiver, filter);
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		super.unregisterReceiver(receiver);
		receivers.remove(receiver);
	}

	public void unregisterAllReceiver() {
		for (BroadcastReceiver br:
			 receivers) {
			super.unregisterReceiver(br);
		}
		receivers.clear();
	}

	@Override
	public PackageManager getPackageManager() {
		if (crackPackageManager) {
			//欺骗MultDexInstaller， 使MultDexInstaller能得到正确的插件信息
			return PluginMultiDexHelper.fixPackageManagerForMultDexInstaller(mPluginDescriptor.getPackageName(), super.getPackageManager());
		}
		return super.getPackageManager();
	}

	public void setCrackPackageManager(boolean crackPackageManager) {
		this.crackPackageManager = crackPackageManager;
	}

	@Override
	public File getFilesDir() {
		return new File(new File(mPluginDescriptor.getInstalledPath()).getParentFile(), "files");
	}
}
