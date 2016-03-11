package com.plugin.core.manager;

import com.plugin.content.PluginDescriptor;
import java.util.Collection;

public interface PluginManager {
	////管理接口
	int installPlugin(String srcFile);
	void loadInstalledPlugins();
	boolean remove(String packageName);
	boolean removeAll();

	////查询接口
	Collection<PluginDescriptor> getPlugins();
	PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId);
	PluginDescriptor getPluginDescriptorByPluginId(String pluginId);
	PluginDescriptor getPluginDescriptorByClassName(String clazzName);

}