package com.plugin.core.manager;

public interface PluginCallback {
	void onPluginLoaderInited();
	void onPluginInstalled(String packageName, String version);
	void onPluginRemoved(String packageName);
	void onPluginStarted(String packageName);
	void onPluginRemoveAll();
}
