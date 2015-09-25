package com.plugin.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.RefInvoker;

public class PluginContextTheme extends PluginBaseContextWrapper {
	private int mThemeResource;
	Resources.Theme mTheme;
	private LayoutInflater mInflater;

	Resources mResources;
	private final ClassLoader mClassLoader;

	private final PluginDescriptor mPluginDescriptor;

	public PluginContextTheme(PluginDescriptor pluginDescriptor, Context base, Resources resources, ClassLoader classLoader) {
		super(base);
		mPluginDescriptor = pluginDescriptor;
		mResources = resources;
		mClassLoader = classLoader;
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

		Object result = RefInvoker.invokeStaticMethod(Resources.class.getName(), "selectDefaultTheme", new Class[] {
				int.class, int.class }, new Object[] { mThemeResource,
				getBaseContext().getApplicationInfo().targetSdkVersion });
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
		return getBaseContext().getSystemService(name);
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
		//如果是独立插件 返回插件本身的packageName
		//但是只返回插件本身的packageName可能会引起其他问题
		//例如1、会导致toast无法弹出，原因是toast弹出时会检查packageName是否时当前用户的
		//   2、导致宿主的Application获取的sharepreference和插件Activity获取的shareperference不在同一个xml里面
		//if (mPluginDescriptor.isStandalone()) {
		//	return mPluginDescriptor.getPackageName();
		//} else {
			return super.getPackageName();
		//}
	}

	@Override
	public String getPackageCodePath() {
		//if (mPluginDescriptor.isStandalone()) {
		//	return mPluginDescriptor.getInstalledPath();
		//} else {
			return super.getPackageCodePath();
		//}
	}

	public PluginDescriptor getPluginDescriptor() {
		return mPluginDescriptor;
	}
}
