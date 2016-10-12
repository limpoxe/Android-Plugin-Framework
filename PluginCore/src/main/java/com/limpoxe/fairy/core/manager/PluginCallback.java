package com.limpoxe.fairy.core.manager;

public interface PluginCallback {

	public static final String ACTION_PLUGIN_CHANGED = "com.limpoxe.fairy.action_plugin_changed";

	void onInstall(int result, String packageName, String version, String src);
	void onRemove(String packageName, boolean success);
	void onRemoveAll(boolean success);

	void onStart(String packageName);
	void onStop(String packageName);
}
