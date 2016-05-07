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
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.view.LayoutInflater;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.core.multidex.PluginMultiDexHelper;
import com.plugin.util.RefInvoker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

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

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 隔离插件间的SharedPreferences
	 * @param name
	 * @param mode
	 * @return
	 */
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {

		//这里之所以需要追加前缀是因为ContextImpl类中的全局静态缓存sSharedPrefs
		if (!name.startsWith(mPluginDescriptor.getPackageName() + "_")) {
			name = mPluginDescriptor.getPackageName() + "_" + name;
		}

		//4.4以上版本缓存是延迟初始化的，这里增加这句调用是为了确保已经初始化，防止反射为空
		PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		Object cache = RefInvoker.getStaticFieldObject("android.app.ContextImpl", "sSharedPrefs");
		if (Build.VERSION.SDK_INT >= 19 && cache instanceof ArrayMap) {
			synchronized (PluginContextTheme.class) {
				ArrayMap<String, ArrayMap<String, Object>> sSharedPrefs = (ArrayMap<String, ArrayMap<String, Object>>)cache;
				final String packageName = getPackageName();
				ArrayMap<String, Object> packagePrefs = sSharedPrefs.get(packageName);
				if (packagePrefs == null) {
					packagePrefs = new ArrayMap<String, Object>();
					sSharedPrefs.put(packageName, packagePrefs);
				}

				Object sp = packagePrefs.get(name);
				if (sp == null) {
					packagePrefs.put(name, newSharedPreferencesImpl(getSharedPrefsFile(name), mode));
				}
			}
		} else if (cache instanceof HashMap) {
			HashMap<String, Object>  sSharedPrefs = (HashMap<String, Object>)cache;
			Object sp = sSharedPrefs.get(name);
			if (sp == null) {
				sSharedPrefs.put(name, newSharedPreferencesImpl(getSharedPrefsFile(name), mode));
			}
		}

		return super.getSharedPreferences(name, mode);
	}

	private Object newSharedPreferencesImpl(File prefsFile, int mode) {
		try {
			Class SharedPreferencesImpl = Class.forName("android.app.SharedPreferencesImpl");
			Constructor constructor = SharedPreferencesImpl.getDeclaredConstructor(File.class, int.class);
			constructor.setAccessible(true);
			return constructor.newInstance(prefsFile, mode);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File getSharedPrefsFile(String name) {
		if (!name.startsWith(mPluginDescriptor.getPackageName() + "_")) {
			name = mPluginDescriptor.getPackageName() + "_" + name;
		}
		return makeFilename(new File(getDataDir(), "shared_prefs"), name + ".xml");
	}

	@Override
	public File getDir(String name, int mode) {
		File dir = makeFilename(getDataDir(), "app_" + name);
		if (!dir.exists()) {
			dir.mkdirs();
			//setpermisssion
		}
		return  dir;
	}

	@Override
	public File getFilesDir() {
		File dir = new File(getDataDir(), "files");
		if (!dir.exists()) {
			dir.mkdirs();
			//setpermisssion
		}
		return dir;
	}

	@Override
	public File getFileStreamPath(String name) {
		return makeFilename(getFilesDir(), name);
	}

	@Override
	public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
		final boolean append = (mode&MODE_APPEND) != 0;
		File f = makeFilename(getFilesDir(), name);
		try {
			FileOutputStream fos = new FileOutputStream(f, append);
			//setFilePermissionsFromMode(f.getPath(), mode, 0);
			return fos;
		} catch (FileNotFoundException e) {
		}
		return super.openFileOutput(name, mode);
	}

	@Override
	public FileInputStream openFileInput(String name) throws FileNotFoundException {
		File f = makeFilename(getFilesDir(), name);
		return new FileInputStream(f);
	}

	@Override
	public File getNoBackupFilesDir() {
		File dir = new File(getDataDir(), "no_backup");
		if (!dir.exists()) {
			dir.mkdirs();
			//setpermisssion
		}
		return dir;
	}

	@Override
	public File getCacheDir() {
		File dir = new File(getDataDir(), "cache");
		if (!dir.exists()) {
			dir.mkdirs();
			//setpermisssion
		}
		return dir;
	}

	@Override
	public File getCodeCacheDir() {
		File dir = new File(getDataDir(), "code_cache");
		if (!dir.exists()) {
			dir.mkdirs();
			//setpermisssion
		}
		return dir;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
		name = getAbsuloteDatabasePath(name);
		return super.openOrCreateDatabase(name, mode, factory);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
											   DatabaseErrorHandler errorHandler) {
		name = getAbsuloteDatabasePath(name);
		return super.openOrCreateDatabase(name, mode, factory, errorHandler);
	}

	@Override
	public boolean deleteDatabase(String name) {
		name = getAbsuloteDatabasePath(name);
		return super.deleteDatabase(name);
	}

	@Override
	public File getDatabasePath(String name) {
		name = getAbsuloteDatabasePath(name);
		return super.getDatabasePath(name);
	}

	@Override
	public String[] databaseList() {
		File f = new File(getDataDir(), "databases");
		final String[] list = f.list();
		return (list != null) ? list : EMPTY_STRING_ARRAY;
	}

	@Override
	public String[] fileList() {
		final String[] list = getFilesDir().list();
		return (list != null) ? list : EMPTY_STRING_ARRAY;
	}

	@Override
	public boolean deleteFile(String name) {
		File f = makeFilename(getFilesDir(), name);
		return f.delete();
	}

	private String getAbsuloteDatabasePath(String name) {
		if (name.charAt(0) != File.separatorChar) {
			File f = makeFilename(new File(getDataDir(), "databases"), name);
			name = f.getAbsolutePath();
		}
		return name;
	}

	private File makeFilename(File base, String name) {
		if (name.indexOf(File.separatorChar) < 0) {
			if (!base.exists()) {
				base.mkdirs();
			}
			return new File(base, name);
		}
		throw new IllegalArgumentException(
				"File " + name + " contains a path separator");
	}

	private static final String[] EMPTY_STRING_ARRAY = {};

	private File dataDir;

	private File getDataDir() {
		if (dataDir == null) {
			dataDir = new File(new File(mPluginDescriptor.getInstalledPath()).getParentFile().getParentFile(), "data");
		}
		return dataDir;
	}

}
