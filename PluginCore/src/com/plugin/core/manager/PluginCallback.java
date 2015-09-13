package com.plugin.core.manager;

import com.plugin.content.PluginDescriptor;

public interface PluginCallback {
	void onPluginLoaderInited();
	void onPluginInstalled(String packageName, String version);
	void onPluginRemoved(String packageName);
	void onPluginStarted(String packageName);
	void onPluginRemoveAll();
}
