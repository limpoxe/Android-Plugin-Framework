package com.plugin.core.manager;

public interface PluginCallback {

	public static final String ACTION_PLUGIN_CHANGED = "com.plugin.core.action_plugin_changed";

	void onPluginLoaderInited();
	void onPluginInstalled(String packageName, String version);
	void onPluginRemoved(String packageName);
	void onPluginStarted(String packageName);
	void onPluginRemoveAll();
}
