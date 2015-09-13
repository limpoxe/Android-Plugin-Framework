package com.plugin.core.manager;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.ManifestParser;
import com.plugin.util.RefInvoker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import dalvik.system.DexClassLoader;

public interface PluginManager {
	////管理接口
	void loadInstalledPlugins();
	boolean addOrReplace(PluginDescriptor pluginDescriptor);
	boolean remove(String packageName);
	boolean removeAll();
	void enablePlugin(String pluginId, boolean enable);

	////查询接口
	Collection<PluginDescriptor> getPlugins();
	PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId);
	PluginDescriptor getPluginDescriptorByPluginId(String pluginId);
	PluginDescriptor getPluginDescriptorByClassName(String clazzName);

}