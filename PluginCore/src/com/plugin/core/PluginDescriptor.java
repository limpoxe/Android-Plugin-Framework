package com.plugin.core;

import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;
import dalvik.system.DexClassLoader;

/**
 * <Pre>
 * {
 * 	"id":123,
 * 	"version":"1.0.1",
 * 	"fragments":{
 * 		"test1":"com.example.plugintest.PluginMustRunInSpec",
 * 		"test2":"com.example.plugintest.PluginRunEasy"
 * 	},
 * 	"activities":{
 * 		"test3":"com.example.plugintest.PluginTextActivity",
 * 	}
 * }
 * @author cailiming
 * </Pre>
 */
public class PluginDescriptor implements Serializable {

	private static final long serialVersionUID = -7545734825911798344L;

	private String id;

	private String version;

	private HashMap<String, String> fragments;

	private HashMap<String, String> activities;

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
}
