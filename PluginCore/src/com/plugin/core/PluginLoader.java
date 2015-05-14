package com.plugin.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
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
	private static final Hashtable<String, PluginDescriptor> sInstalledPlugins = new Hashtable<String, PluginDescriptor>();
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
	
	public static Application getApplicatoin() {
		return sApplication;
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
		if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getId())) {
			return isInstallSuccess;
		}
		
		// 第二步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getId());
		if (oldPluginDescriptor != null) {
			remove(pluginDescriptor.getId());
		}
		
		// 第三步骤，复制插件到插件目录
		if (pluginDescriptor != null) {
			
			String destPluginFile = genInstallPath(pluginDescriptor.getId(), pluginDescriptor.getVersion());
			boolean isCopySuccess = ApkReader.copyFile(srcPluginFile, destPluginFile);
			
			// 第四步 添加到已安装插件列表
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

		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassId(clazzId);
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
	
	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassByName(String clazzName) {
		Log.v(LOG_TAG, "loadPluginClass for clazzName " + clazzName);

		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazzName);
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				initPlugin(pluginDescriptor);
				pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			}

			if (pluginClassLoader != null) {

				try {
					Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
					Log.v(LOG_TAG, "loadPluginClass Success for clazzName " + clazzName);
					return pluginClazz;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			
			}
		}

		Log.e(LOG_TAG, "loadPluginClass Fail for clazzName " + clazzName);
		return null;

	}

	/**
	 * 获取当前class所在插件的Context
	 * 每个插件只有1个DefaultContext,是当前插件中所有class公用的Context
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getDefaultPluginContext(@SuppressWarnings("rawtypes") Class clazz) {

		// clazz.getClassLoader(); 直接获取classloader的方式，
		// 如果同一个插件安装两次，但是宿主程序进程没有重启，那么得到的classloader可能是前次安装时的loader
		Context pluginContext = null;
		PluginDescriptor pluginDescriptor = getPluginDescriptorByClassName(clazz.getName());
		if (pluginDescriptor != null) {
			pluginContext = pluginDescriptor.getPluginContext();
		} else {
			Log.e(LOG_TAG, "PluginDescriptor Not Found for " + clazz.getName());
		}
		
		if (pluginContext == null) {
			Log.e(LOG_TAG, "Context Not Found for " + clazz.getName());
		}

		return pluginContext;

	}
	
	/**
	 * 获取当前class所在插件的Context
	 * 为当前 插件class创建一个单独的context
	 * @param clazz
	 * @return
	 */
	public static Context getNewPluginContext(@SuppressWarnings("rawtypes") Class clazz) {

		Context pluginContext = getDefaultPluginContext(clazz);
		//为了使5.0支持主题， 这里做api版本区分
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT <= 20 ) {
			
			if (pluginContext != null) {
				pluginContext = PluginCreator.createPluginApplicationContext(sApplication,
						pluginContext.getResources(), (DexClassLoader)pluginContext.getClassLoader());
				pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
			}
			
			return pluginContext;
			
		} else {
			PluginDescriptor pd = getPluginDescriptorByClassName(clazz.getName());

			Resources resFor5 = PluginCreator.createPluginResourceFor5(sApplication, pd.getInstalledPath());
			
			if (pluginContext != null) {
				pluginContext = PluginCreator.createPluginApplicationContext(sApplication,
						resFor5, (DexClassLoader)pluginContext.getClassLoader());
				pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
			}
			return pluginContext;
		}
		
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
		pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		pluginDescriptor.setPluginContext(pluginContext);
		pluginDescriptor.setPluginClassLoader(pluginClassLoader);
	}

	private static synchronized boolean saveInstalledPlugins(Hashtable<String, PluginDescriptor> installedPlugins) {
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
	public static synchronized void removeAll() {
		sInstalledPlugins.entrySet().iterator();
		
		sInstalledPlugins.clear();
		boolean isSuccess = saveInstalledPlugins(sInstalledPlugins);
		if (isSuccess) {
			Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
			intent.putExtra(EXTRA_TYPE, "remove");
			sApplication.sendBroadcast(intent);
		}
	}
	
	public static synchronized void remove(String pluginId) {
		PluginDescriptor old = sInstalledPlugins.remove(pluginId);
		if (old != null) {
			
			boolean isSuccess = saveInstalledPlugins(sInstalledPlugins);
			
			new File(old.getInstalledPath()).delete();
			
			if (isSuccess) {
				Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
				intent.putExtra(EXTRA_TYPE, "remove");
				sApplication.sendBroadcast(intent);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Hashtable<String, PluginDescriptor> listAll() {
		return (Hashtable<String, PluginDescriptor>) sInstalledPlugins.clone();
	}

	public static PluginDescriptor getPluginDescriptorByClassId(String clazzID) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.getFragments().containsKey(clazzID) && descriptor.isEnabled()) {
				return descriptor;
			} else if (descriptor.getActivities().containsKey(clazzID) && descriptor.isEnabled()) {
				return descriptor;
			}
		}
		return null;
	}

	public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && pluginDescriptor.isEnabled()) {
			return pluginDescriptor;
		} 
		return null;
	}
	
	private static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.getFragments().containsValue(clazzName) && descriptor.isEnabled()) {
				return descriptor;
			} else if (descriptor.getActivities().containsValue(clazzName) && descriptor.isEnabled()) {
				return descriptor;
			}
		}
		return null;
	}

	public static synchronized void enablePlugin(String pluginId, boolean enable) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && !pluginDescriptor.isEnabled()) {
			pluginDescriptor.setEnabled(enable);
			saveInstalledPlugins(sInstalledPlugins);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static synchronized Hashtable<String, PluginDescriptor> readInstalledPlugins() {
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
