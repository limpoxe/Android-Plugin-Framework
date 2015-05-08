package com.plugin.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.plugin.util.ApkReader;

import dalvik.system.DexClassLoader;

public class PluginLoader {
	private static final String LOG_TAG = PluginLoader.class.getSimpleName();
	private static Application sApplication;
	private static final HashMap<String, PluginDescriptor> sInstalledPlugins = new HashMap<String, PluginDescriptor>();
	private static boolean isInited = false;

	public static final String ACTION_PLUGIN_CHANGED = "com.plugin.core.action_plugin_changed";
	public static final String EXTRA_TYPE = "com.plugin.core.EXTRA_TYPE";
	
	private PluginLoader() {
	}

	/**
	 * 初始化loader, 只可调用一次
	 * 
	 * @param app
	 */
	public static synchronized void initLoader(Application app) {
		if (!isInited) {
			sApplication = app;
			readInstalledPlugins();
			isInited = true;
		}
	}

	public boolean isInstalled(String pluginId, String pluginVersion) {
		PluginDescriptor pluginDescriptor  = getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
			return pluginDescriptor.getVersion().equals(pluginVersion);
		}
		return false;
	}
	
	/**
	 * 安装一个插件
	 * 
	 * @param srcPluginFile
	 * @return
	 */
	public static synchronized boolean installPlugin(String srcPluginFile) {
		Log.e(LOG_TAG, "Install plugin " + srcPluginFile);
		
		boolean isInstallSuccess = false;
		// 第一步，读取插件描述文件
		PluginDescriptor pluginDescriptor = ApkReader.readPluginDescriptor(srcPluginFile);
		// 第二步骤，复制插件到插件目录
		if (pluginDescriptor != null) {
			String destPluginFile = genInstallPath(pluginDescriptor.getId(), pluginDescriptor.getVersion());
			boolean isCopySuccess = ApkReader.copyFile(srcPluginFile, destPluginFile);
			// 第三步 添加到已安装插件列表
			if (isCopySuccess) {
				pluginDescriptor.setInstalledPath(destPluginFile);
				PluginDescriptor previous = sInstalledPlugins.put(pluginDescriptor.getId(), pluginDescriptor);
				isInstallSuccess = saveInstalledPlugins(sInstalledPlugins);
				
				if (isInstallSuccess) {
					Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
					if (previous == null) {
						intent.putExtra(EXTRA_TYPE, "add");
					} else {
						intent.putExtra(EXTRA_TYPE, "replace");
					}
					intent.putExtra("id", pluginDescriptor.getId());
					intent.putExtra("version", pluginDescriptor.getVersion());
					sApplication.sendBroadcast(intent);
				}
			}
		}
	
		return isInstallSuccess;
	}

	private static String getPluginClassNameById(PluginDescriptor pluginDescriptor, String clazzId) {
		String clazzName = pluginDescriptor.getFragments().get(clazzId);
		if (clazzName == null) {
			clazzName = pluginDescriptor.getActivities().get(clazzId);
		}
		return clazzName;
	}

	/**
	 * 根据插件中的classId加载一个插件中的class
	 * 
	 * @param clazzId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassById(String clazzId) {
		Log.v(LOG_TAG, "loadPluginClass for clazzId " + clazzId);

		PluginDescriptor pluginDescriptor = getPluginDescriptorById(clazzId);
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				initPlugin(pluginDescriptor);
				pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			}

			if (pluginClassLoader != null) {
				String clazzName = getPluginClassNameById(pluginDescriptor, clazzId);
				if (clazzName != null) {
					try {
						Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
						Log.v(LOG_TAG, "loadPluginClass Success for classId " + clazzId);
						return pluginClazz;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Log.e(LOG_TAG, "loadPluginClass Fail for classId " + clazzId);
		return null;

	}

	/**
	 * 获取当前class所在插件的Context
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getPluginContext(@SuppressWarnings("rawtypes") Class clazz) {

		// clazz.getClassLoader(); 直接获取classloader的方式，
		// 如果同一个插件安装两次，但是宿主程序进程没有重启，那么得到的classloader可能是前次安装时的loader
		Context pluginContext = null;
		PluginDescriptor pluginDescriptor = getPluginDescriptorByName(clazz.getName());
		if (pluginDescriptor != null) {
			pluginContext = pluginDescriptor.getPluginContext();
		}

		if (pluginContext == null) {
			Log.e(LOG_TAG, "Context Not Found for " + clazz.getName());
		}

		return pluginContext;

	}

	/**
	 * 构造插件信息
	 * 
	 * @param pluginClassBean
	 */
	private static void initPlugin(PluginDescriptor pluginDescriptor) {

		Log.d(LOG_TAG, "initPlugin, Resources, DexClassLoader, Context");

		Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor.getInstalledPath());
		DexClassLoader pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath());
		Context pluginContext = PluginCreator
				.createPluginApplicationContext(sApplication, pluginRes, pluginClassLoader);

		pluginDescriptor.setPluginContext(pluginContext);
		pluginDescriptor.setPluginClassLoader(pluginClassLoader);
	}

	private static synchronized boolean saveInstalledPlugins(HashMap<String, PluginDescriptor> installedPlugins) {
		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(installedPlugins);
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

	/**
	 * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
	 */
	public static void removeAll() {
		sInstalledPlugins.clear();
		boolean isSuccess = saveInstalledPlugins(sInstalledPlugins);
		if (isSuccess) {
			Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
			intent.putExtra(EXTRA_TYPE, "remove");
			sApplication.sendBroadcast(intent);
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, PluginDescriptor> listAll() {
		return (HashMap<String, PluginDescriptor>) sInstalledPlugins.clone();
	}

	private static PluginDescriptor getPluginDescriptorById(String clazzID) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.getFragments().containsKey(clazzID)) {
				return descriptor;
			} else if (descriptor.getActivities().containsKey(clazzID)) {
				return descriptor;
			}
		}
		return null;
	}

	public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {

		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.getId().equals(pluginId)) {
				return descriptor;
			}
		}
		return null;
	
	}
	
	private static PluginDescriptor getPluginDescriptorByName(String clazzName) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.getFragments().containsValue(clazzName)) {
				return descriptor;
			} else if (descriptor.getActivities().containsValue(clazzName)) {
				return descriptor;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static synchronized HashMap<String, PluginDescriptor> readInstalledPlugins() {
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
				sInstalledPlugins.putAll((HashMap<String, PluginDescriptor>) object);
			}
		}
		return sInstalledPlugins;
	}

	/**
	 * 插件的安装目录, 插件apk将来会被放在这个目录下面
	 */
	private static String genInstallPath(String pluginId, String pluginVersoin) {
		return sApplication.getDir("plugin_dir", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginId + "/"
				+ pluginVersoin + ".apk";
	}

	private static SharedPreferences getSharedPreference() {
		SharedPreferences sp = sApplication.getSharedPreferences("plugins.installed", 
				Build.VERSION.SDK_INT < 11 ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | 0x0004);
		return sp;
	}

}
