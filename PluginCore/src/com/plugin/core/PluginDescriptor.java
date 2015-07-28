package com.plugin.core;

import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;

/**
 * <Pre>
 * {
 * 	"id":123,
 * 	"version":"1.0.1",
 *  "description":"插件介绍",
 *  "application":"com.example.plugintest.PluginApplication",
 * 	"fragments":{
 * 		"test1":"com.example.plugintest.PluginMustRunInSpec",
 * 		"test2":"com.example.plugintest.PluginRunEasy"
 * 	},
 * 	"activities":{
 * 		"test3":"com.example.plugintest.PluginTextActivity",
 * 	},
 *  "services":{
 *  	"test4":"com.example.plugintest.PluginTextService"
 *  } 	
 * }
 * @author cailiming
 * </Pre>
 */
public class PluginDescriptor implements Serializable {

	private static final long serialVersionUID = -7545734825911798344L;

	private String id;

	private String version;

	private String description;

	private boolean isEnabled;

	private String application;
	
	private HashMap<String, String> fragments;

	private HashMap<String, String> activities;

	private HashMap<String, String> services;
	
	private String installedPath;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public HashMap<String, String> getActivities() {
		return activities;
	}

	public void setActivities(HashMap<String, String> activities) {
		this.activities = activities;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
	
	public HashMap<String, String> getServices() {
		return services;
	}

	public void setServices(HashMap<String, String> services) {
		this.services = services;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public String getPluginClassNameById(String clazzId) {
		String clazzName = getFragments().get(clazzId);
		if (clazzName == null) {
			clazzName = getActivities().get(clazzId);
		}
		if (clazzName == null) {
			clazzName = getServices().get(clazzId);
		}
		
		if (clazzName == null) {
			Log.d("PluginDescriptor", "clazzName not found for classId " + clazzId);
		} else {
			Log.d("PluginDescriptor", "clazzName found: " + clazzName);
		}
		return clazzName;
	}
	
	public boolean containsId(String clazzId) {
		if (getFragments().containsKey(clazzId) && isEnabled()) {
			return true;
		} else if (getActivities().containsKey(clazzId) && isEnabled()) {
			return true;
		} else if (getServices().containsKey(clazzId) && isEnabled()) {
			return true;
		}
		return false;
	}
	
	public boolean containsName(String clazzName) {
		if (getFragments().containsValue(clazzName) && isEnabled()) {
			return true;
		} else if (getActivities().containsValue(clazzName) && isEnabled()) {
			return true;
		} else if (getServices().containsValue(clazzName) && isEnabled()) {
			return true;
		}
		return false;
	}
}
