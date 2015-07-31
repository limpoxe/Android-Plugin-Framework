package com.plugin.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.plugin.util.LogUtil;

import android.app.Application;
import android.content.Context;
import dalvik.system.DexClassLoader;

/**
 * <Pre>
 * @author cailiming
 * </Pre>
 */
public class PluginDescriptor implements Serializable {

	private static final long serialVersionUID = -7545734825911798344L;

	private String packageName;

	private String version;

	private String description;

	private boolean isStandalone;

	private boolean isEnabled;

	private String applicationName;
	
	private HashMap<String, String> fragments = new HashMap<String, String>();

	private HashMap<String, ArrayList<PluginIntentFilter>> components;

	private String installedPath;

	private transient Application pluginApplication;
	
	private transient DexClassLoader pluginClassLoader;

	private transient Context pluginContext;

	public Context getPluginContext() {
		return pluginContext;
	}

	public void setPluginContext(Context pluginContext) {
		this.pluginContext = pluginContext;
	}

	public DexClassLoader getPluginClassLoader() {
		return pluginClassLoader;
	}

	public void setPluginClassLoader(DexClassLoader pluginLoader) {
		this.pluginClassLoader = pluginLoader;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HashMap<String, String> getFragments() {
		return fragments;
	}

	public void setfragments(HashMap<String, String> fragments) {
		this.fragments = fragments;
	}

	public String getInstalledPath() {
		return installedPath;
	}

	public void setInstalledPath(String installedPath) {
		this.installedPath = installedPath;
	}

	public HashMap<String, ArrayList<PluginIntentFilter>> getComponents() {
		return components;
	}

	public void setComponents(HashMap<String, ArrayList<PluginIntentFilter>> activities) {
		this.components = activities;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	/**
	 * 需要根据id查询的只有fragment
	 * @param clazzId
	 * @return
	 */
	public String getPluginClassNameById(String clazzId) {
		String clazzName = getFragments().get(clazzId);

		if (clazzName == null) {
			LogUtil.d("PluginDescriptor", "clazzName not found for classId ", clazzId);
		} else {
			LogUtil.d("PluginDescriptor", "clazzName found ", clazzName);
		}
		return clazzName;
	}
	
	public Application getPluginApplication() {
		return pluginApplication;
	}

	public void setPluginApplication(Application pluginApplication) {
		this.pluginApplication = pluginApplication;
	}

	public boolean isStandalone() {
		return isStandalone;
	}

	public void setStandalone(boolean isStandalone) {
		this.isStandalone = isStandalone;
	}
	
	/**
	 * 需要根据Id查询的只有fragment
	 * @param clazzId
	 * @return
	 */
	public boolean containsFragment(String clazzId) {
		if (getFragments().containsKey(clazzId) && isEnabled()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 根据className查询
	 * @param clazzName
	 * @return
	 */
	public boolean containsName(String clazzName) {
		if (getFragments().containsValue(clazzName) && isEnabled()) {
			return true;
		} else if (getComponents().containsKey(clazzName) && isEnabled()) {
			return true;
		}
		return false;
	}
}
