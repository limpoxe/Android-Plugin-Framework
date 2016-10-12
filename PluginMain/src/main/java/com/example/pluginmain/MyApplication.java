package com.example.pluginmain;

import com.plugin.core.PluginApplication;
import com.plugin.core.PluginLoader;
import com.plugin.util.ProcessUtil;

public class MyApplication extends PluginApplication {
	@Override
	public void onCreate() {
		super.onCreate();

		//可选, 指定loading页UI, 用于首次加载插件时, 显示菊花等待插件加载完毕,
		if (ProcessUtil.isPluginProcess(this)) {
			PluginLoader.setLoadingResId(R.layout.loading);
		}
	}
}
