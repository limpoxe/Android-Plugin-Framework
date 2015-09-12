package com.plugin.core;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

public class PluginApplication extends Application {

	@Override
	protected void attachBaseContext(Context base) {

		super.attachBaseContext(base);

		PluginLoader.initLoader(this);
	}

	/**
	 * 重写这个方法是为了支持Receiver
	 *
	 * @return
	 */
	@Override
	public Context getBaseContext() {
		return ((ContextWrapper)super.getBaseContext()).getBaseContext();
	}
}
