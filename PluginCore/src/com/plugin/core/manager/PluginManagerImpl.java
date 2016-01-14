package com.plugin.core.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class PluginManagerImpl implements PluginManager {

	private static final String INSTALLED_KEY = "plugins.list";

	private static final String PENDING_KEY = "plugins.pending";

	private final Hashtable<String, PluginDescriptor> sInstalledPlugins = new Hashtable<String, PluginDescriptor>();

	private final Hashtable<String, PluginDescriptor> sPendingPlugins = new Hashtable<String, PluginDescriptor>();

	/**
	 * 插件的安装目录, 插件apk将来会被放在这个目录下面
	 */
	@Override
	public String genInstallPath(String pluginId, String pluginVersoin) {
		return PluginLoader.getApplicatoin().getDir("plugin_dir", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginId + "/"
				+ pluginVersoin + "/base-1.apk";
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void loadInstalledPlugins() {
		if (sInstalledPlugins.size() == 0) {
			Hashtable<String, PluginDescriptor> installedPlugin = readPlugins(INSTALLED_KEY);
			if (installedPlugin != null) {
				sInstalledPlugins.putAll(installedPlugin);
			}

			//把pending合并到install
			Hashtable<String, PluginDescriptor> pendingPlugin = readPlugins(PENDING_KEY);
			if (pendingPlugin != null) {
				Iterator<Map.Entry<String, PluginDescriptor>> itr = pendingPlugin.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, PluginDescriptor> entry = itr.next();
					//删除旧版
					remove(entry.getKey());
				}

				//保存新版
				sInstalledPlugins.putAll(pendingPlugin);
				savePlugins(INSTALLED_KEY, sInstalledPlugins);

				//清除pending
				getSharedPreference().edit().remove(PENDING_KEY).commit();
			}
		}
	}

	@Override
	public boolean addOrReplace(PluginDescriptor pluginDescriptor) {
		sInstalledPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
		return savePlugins(INSTALLED_KEY, sInstalledPlugins);
	}

	@Override
	public boolean pending(PluginDescriptor pluginDescriptor) {
		sPendingPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
		return savePlugins(PENDING_KEY, sPendingPlugins);
	}

	@Override
	public synchronized boolean removeAll() {
		sInstalledPlugins.clear();
		boolean isSuccess = savePlugins(INSTALLED_KEY, sInstalledPlugins);
		return isSuccess;
	}

	@Override
	public synchronized boolean remove(String pluginId) {
		PluginDescriptor old = sInstalledPlugins.remove(pluginId);
		if (old != null) {
			boolean isSuccess = savePlugins(INSTALLED_KEY, sInstalledPlugins);
			boolean deleteSuccess = FileUtil.deleteAll(new File(old.getInstalledPath()).getParentFile());
			LogUtil.d("delete old", isSuccess, deleteSuccess, old.getInstalledPath(), old.getPackageName());
			return isSuccess;
		}
		return false;
	}

	@Override
	public Collection<PluginDescriptor> getPlugins() {
		return sInstalledPlugins.values();
	}

	@Override
	public synchronized void enablePlugin(String pluginId, boolean enable) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && !pluginDescriptor.isEnabled()) {
			pluginDescriptor.setEnabled(enable);
			savePlugins(INSTALLED_KEY, sInstalledPlugins);
		}
	}

	/**
	 * for Fragment
	 *
	 * @param clazzId
	 * @return
	 */
	@Override
	public PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsFragment(clazzId)) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && pluginDescriptor.isEnabled()) {
			return pluginDescriptor;
		}
		return null;
	}

	@Override
	public PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsName(clazzName)) {
				return descriptor;
			}
		}
		return null;
	}

	private static SharedPreferences getSharedPreference() {
		SharedPreferences sp = PluginLoader.getApplicatoin().getSharedPreferences("plugins.installed",
				Build.VERSION.SDK_INT < 11 ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | 0x0004);
		return sp;
	}

	private synchronized boolean savePlugins(String key, Hashtable<String, PluginDescriptor> plugins) {

		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(plugins);
			objectOutputStream.flush();

			byte[] data = byteArrayOutputStream.toByteArray();
			String list = Base64.encodeToString(data, Base64.DEFAULT);

			getSharedPreference().edit().putString(key, list).commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (byteArrayOutputStream != null) {
				try {
					byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private synchronized Hashtable<String, PluginDescriptor> readPlugins(String key) {
		String list = getSharedPreference().getString(key, "");
		Serializable object = null;
		if (!TextUtils.isEmpty(list)) {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					Base64.decode(list, Base64.DEFAULT));
			ObjectInputStream objectInputStream = null;
			try {
				objectInputStream = new ObjectInputStream(byteArrayInputStream);
				object = (Serializable) objectInputStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (objectInputStream != null) {
					try {
						objectInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (byteArrayInputStream != null) {
					try {
						byteArrayInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return (Hashtable<String, PluginDescriptor>) object;
	}

}