package com.plugin.core.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.plugin.core.PluginContextTheme;
import com.plugin.core.PluginLoader;
/**
 * 重写过context的Activity 用来展示fragment
 * @author cailiming
 *
 */
public class PluginSpecDisplayer extends PluginNormalDisplayer {
	private static final String LOG_TAG = PluginSpecDisplayer.class.getSimpleName();

	private Context mOrignalContext;
	private int mOrignalThemeResourceId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context pctx = findPluginContext();
		bindPluginContext(pctx);

		super.onCreate(savedInstanceState);
	}

	private Context findPluginContext() {
		String classId = getIntent().getStringExtra("classId");
		Log.v(LOG_TAG, "findPluginContext " + classId);
		@SuppressWarnings("rawtypes")
		Class clazz = PluginLoader.loadPluginClassById(classId);

		if (clazz != null) {
			return PluginLoader.getDefaultPluginContext(clazz);
		}
		return null;
	}

	private void bindPluginContext(Context pctx) {
		if (pctx != null) {
			super.attachBaseContext(new PluginContextTheme(mOrignalContext, pctx.getResources(), pctx.getClassLoader()));
			if (mOrignalThemeResourceId != 0) {
				super.setTheme(mOrignalThemeResourceId);
			}
		} else {
			super.attachBaseContext(mOrignalContext);
		}
	}

	@Override
	public void setTheme(int resid) {
		if (getBaseContext() == null) {
			mOrignalThemeResourceId = resid;
		} else {
			super.setTheme(resid);
		}
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		mOrignalContext = newBase;
	}

	@Override
	public Context getApplicationContext() {
		if (getBaseContext() == null) {
			return mOrignalContext.getApplicationContext();
		} else {
			return super.getApplicationContext();
		}
	}

	@Override
	public Object getSystemService(String name) {
		if (getBaseContext() == null) {
			return mOrignalContext.getSystemService(name);
		} else {
			return super.getSystemService(name);
		}
	}

	@Override
	public Resources getResources() {
		if (Build.VERSION.SDK_INT < 14) {
			if (getBaseContext() == null) {
				return mOrignalContext.getResources();
			} else {
				return super.getResources();
			}
		} else {
			return super.getResources();
		}
	}
}
