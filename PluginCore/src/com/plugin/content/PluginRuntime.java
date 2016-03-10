package com.plugin.content;


import java.io.Serializable;
import java.util.HashMap;

/**
 * <Pre>
 * @author cailiming
 * </Pre>
 *
 */
public class PluginRuntime implements Serializable {

	private static PluginRuntime runtime;

	private HashMap<String, LoadedPlugin> loadedPluginMap = new HashMap<String, LoadedPlugin>();

	private PluginRuntime() {
	}

	public static PluginRuntime instance() {
		if (runtime == null) {
			synchronized (PluginRuntime.class) {
				if (runtime == null) {
					runtime = new PluginRuntime();
				}
			}
		}
		return runtime;
	}

	public LoadedPlugin getLoadedPlugin(String packageName) {
		return loadedPluginMap.get(packageName);
	}

	public void startPlugin() {

	}

	public void stopPlugin() {

	}
}
