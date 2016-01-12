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

public class PluginManagerImpl implements PluginManager {

	private final Hashtable<String, PluginDescriptor> sInstalledPlugins = new Hashtable<String, PluginDescriptor>();

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
			// 读取已经安装的插件列表
			String list = getSharedPreference().getString("plugins.list", "");
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
			if (object != null) {

				Hashtable<String, PluginDescriptor> installedPlugin = (Hashtable<String, PluginDescriptor>) object;
				sInstalledPlugins.putAll(installedPlugin);
			}
		}
	}

	@Override
	public boolean addOrReplace(PluginDescriptor pluginDescriptor) {
		sInstalledPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
		return saveInstalledPlugins();
	}

	@Override
	public synchronized boolean removeAll() {
		sInstalledPlugins.clear();
		boolean isSuccess = saveInstalledPlugins();
		return isSuccess;
	}

	@Override
	public synchronized boolean remove(String pluginId) {
		PluginDescriptor old = sInstalledPlugins.remove(pluginId);
		if (old != null) {
			boolean isSuccess = saveInstalledPlugins();
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
			saveInstalledPlugins();
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

	private synchronized boolean saveInstalledPlugins() {

		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(sInstalledPlugins);
			objectOutputStream.flush();

			byte[] data = byteArrayOutputStream.toByteArray();
			String list = Base64.encodeToString(data, Base64.DEFAULT);

			getSharedPreference().edit().putString("plugins.list", list).commit();
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

}