package com.plugin.core.manager;

import com.plugin.content.PluginDescriptor;
import java.util.Collection;

public interface PluginManager {
	////管理接口
	void loadInstalledPlugins();
	boolean addOrReplace(PluginDescriptor pluginDescriptor);
	boolean remove(String packageName);
	boolean removeAll();
	void enablePlugin(String pluginId, boolean enable);
	String genInstallPath(String pluginId, String pluginVersoin);

	////查询接口
	Collection<PluginDescriptor> getPlugins();
	PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId);
	PluginDescriptor getPluginDescriptorByPluginId(String pluginId);
	PluginDescriptor getPluginDescriptorByClassName(String clazzName);

}