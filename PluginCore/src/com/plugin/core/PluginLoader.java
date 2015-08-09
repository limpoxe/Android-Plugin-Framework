package com.plugin.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.util.LogUtil;
import com.plugin.util.ManifestParser;
import com.plugin.util.FileUtil;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

public class PluginLoader {
	public static final String ACTION_PLUGIN_CHANGED = "com.plugin.core.action_plugin_changed";
	public static final String EXTRA_TYPE = "com.plugin.core.EXTRA_TYPE";

	private static Application sApplication;
	private static boolean isInited = false;
	private static final Hashtable<String, PluginDescriptor> sInstalledPlugins = new Hashtable<String, PluginDescriptor>();

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
		PluginDescriptor pluginDescriptor = getPluginDescriptorByPluginId(pluginId);
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
		LogUtil.d("Install plugin ", srcPluginFile);

		boolean isInstallSuccess = false;
		// 第一步，读取插件描述文件
		PluginDescriptor pluginDescriptor = ManifestParser.parseManifest(srcPluginFile);
		if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getPackageName())) {
			return isInstallSuccess;
		}

		// 第二步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
		if (oldPluginDescriptor != null) {
			remove(pluginDescriptor.getPackageName());
		}

		// 第三步骤，复制插件到插件目录
		if (pluginDescriptor != null) {

			String destPluginFile = genInstallPath(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destPluginFile);

			// 第四步 添加到已安装插件列表
			if (isCopySuccess) {
				pluginDescriptor.setInstalledPath(destPluginFile);
				PluginDescriptor previous = sInstalledPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
				isInstallSuccess = saveInstalledPlugins(sInstalledPlugins);

				if (isInstallSuccess) {
					Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
					if (previous == null) {
						intent.putExtra(EXTRA_TYPE, "add");
					} else {
						intent.putExtra(EXTRA_TYPE, "replace");
					}
					intent.putExtra("id", pluginDescriptor.getPackageName());
					intent.putExtra("version", pluginDescriptor.getVersion());
					sApplication.sendBroadcast(intent);
				}
			}
		}

		return isInstallSuccess;
	}

	/**
	 * 根据插件中的classId加载一个插件中的class
	 * 
	 * @param clazzId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassById(String clazzId) {
		LogUtil.d("loadPluginClass for clazzId ", clazzId);

		PluginDescriptor pluginDescriptor = getPluginDescriptorByFragmenetId(clazzId);
		if (pluginDescriptor != null) {
			DexClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			if (pluginClassLoader == null) {
				initPlugin(pluginDescriptor);
				pluginClassLoader = pluginDescriptor.getPluginClassLoader();
			}

			if (pluginClassLoader != null) {
				String clazzName = pluginDescriptor.getPluginClassNameById(clazzId);
				LogUtil.d("loadPluginClass clazzName=", clazzName);
				if (clazzName != null) {
					try {
						Class pluginClazz = ((ClassLoader) pluginClassLoader).loadClass(clazzName);
						LogUtil.d("loadPluginClass for classId ", clazzId, " Success");
						return pluginClazz;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		LogUtil.d("loadPluginClass for classId ", clazzId, " Fail");
		return null;

	}

	@SuppressWarnings("rawtypes")
	public static Class loadPluginClassByName(String clazzName) {
		LogUtil.d("loadPluginClass for clazzName ", clazzName);

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
					LogUtil.d("loadPluginClass Success for clazzName ", clazzName);
					return pluginClazz;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		}

		LogUtil.d("loadPluginClass Fail for clazzName ", clazzName);
		return null;

	}

	/**
	 * 获取当前class所在插件的Context 每个插件只有1个DefaultContext,是当前插件中所有class公用的Context
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
			LogUtil.d("PluginDescriptor Not Found for ", clazz.getName());
		}

		if (pluginContext == null) {
			LogUtil.d("Context Not Found for ", clazz.getName());
		}

		return pluginContext;

	}

	/**
	 * 获取当前class所在插件的Context 为当前 插件class创建一个单独的context
	 * 
	 * @param clazz
	 * @return
	 */
	public static Context getNewPluginContext(@SuppressWarnings("rawtypes") Class clazz) {
		Context pluginContext = getDefaultPluginContext(clazz);
		if (pluginContext != null) {
			pluginContext = PluginCreator.createPluginApplicationContext(sApplication, pluginContext.getResources(),
					(DexClassLoader) pluginContext.getClassLoader());
			pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		}
		return pluginContext;
	}

	/**
	 * 构造插件信息
	 * 
	 * @param pluginClassBean
	 */
	private static void initPlugin(PluginDescriptor pluginDescriptor) {

		LogUtil.d("initPlugin, Resources, DexClassLoader, Context, Application ", pluginDescriptor.getApplicationName());

		LogUtil.d("是否为独立插件", pluginDescriptor.isStandalone());

		Resources pluginRes = PluginCreator.createPluginResource(sApplication, pluginDescriptor.getInstalledPath(),
				pluginDescriptor.isStandalone());

		DexClassLoader pluginClassLoader = PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(),
				pluginDescriptor.isStandalone());
		Context pluginContext = PluginCreator
				.createPluginApplicationContext(sApplication, pluginRes, pluginClassLoader);

		pluginContext.setTheme(sApplication.getApplicationContext().getApplicationInfo().theme);
		pluginDescriptor.setPluginContext(pluginContext);
		pluginDescriptor.setPluginClassLoader(pluginClassLoader);

		checkPluginPublicXml(pluginDescriptor, pluginRes);

		callPluginApplicationOncreate(pluginDescriptor);
	}

	private static void callPluginApplicationOncreate(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor.getApplicationName() != null && pluginDescriptor.getPluginApplication() == null
				&& pluginDescriptor.getPluginClassLoader() != null) {
			try {
				Class pluginApplicationClass = ((ClassLoader) pluginDescriptor.getPluginClassLoader())
						.loadClass(pluginDescriptor.getApplicationName());
				Application application = (Application) pluginApplicationClass.newInstance();

				RefInvoker.invokeMethod(application, "android.app.Application", "attach",
						new Class[] { Context.class }, new Object[] { sApplication });

				LogUtil.d("初始化插件apk的application对象", pluginDescriptor.getApplicationName());
				pluginDescriptor.setPluginApplication(application);
				application.onCreate();

				Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
				intent.putExtra(EXTRA_TYPE, "inited");
				sApplication.sendBroadcast(intent);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean checkPluginPublicXml(PluginDescriptor pluginDescriptor, Resources res) {

		// "plugin_layout_1"资源id时由public.xml配置的
		// 如果没有检测到这个资源，说明编译时没有引入public.xml,
		// 这里直接抛个异常出去。
		// 不同的系统版本获取id的方式不同，
		// 三星4.x等系统适用
		int publicStub = res.getIdentifier("plugin_layout_1", "layout", pluginDescriptor.getPackageName());
		if (publicStub == 0) {
			// 小米5.x等系统适用
			publicStub = res.getIdentifier("plugin_layout_1", "layout", sApplication.getPackageName());
		}
		if (publicStub == 0) {
			try {
				// 如果以上两种方式都检测失败，最后尝试通过反射检测
				Class layoutClass = ((ClassLoader) pluginDescriptor.getPluginClassLoader()).loadClass(pluginDescriptor
						.getPackageName() + ".R$layout");
				Integer layouId = (Integer) RefInvoker.getFieldObject(null, layoutClass, "plugin_layout_1");
				if (layouId != null) {
					publicStub = layouId;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (publicStub == 0) {
			throw new IllegalStateException("\n插件工程没有使用public.xml给资源id分组！！！\n" + "插件工程没有使用public.xml给资源id分组！！！\n"
					+ "插件工程没有使用public.xml给资源id分组！！！\n" + "重要的事情讲三遍！！！");
		}
		return true;
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

	/**
	 * for Fragment
	 * 
	 * @param clazzId
	 * @return
	 */
	public static PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsFragment(clazzId)) {
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

	public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsName(clazzName)) {
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

	/**
	 * //If getComponent returns an explicit class, that is returned without any
	 * further consideration. //If getAction is non-NULL, the activity must
	 * handle this action. //If resolveType returns non-NULL, the activity must
	 * handle this type. //If addCategory has added any categories, the activity
	 * must handle ALL of the categories specified. //If getPackage is non-NULL,
	 * only activity components in that application package will be considered.
	 * 
	 * @param intent
	 * @return
	 */
	public static String isMatchPlugin(Intent intent) {

		Hashtable<String, PluginDescriptor> plugins = listAll();

		Iterator<PluginDescriptor> itr = plugins.values().iterator();

		while (itr.hasNext()) {
			PluginDescriptor plugin = itr.next();
			// 如果是通过组件进行匹配的
			if (intent.getComponent() != null) {
				if (plugin.containsName(intent.getComponent().getClassName())) {
					LogUtil.d("PluginLoader", "通过Component进行匹配成功，", intent.getComponent().getClassName());
					return intent.getComponent().getClassName();
				}
			} else {
				// 如果是通过IntentFilter进行匹配的
				HashMap<String, ArrayList<PluginIntentFilter>> intentFilter = plugin.getComponents();
				if (intentFilter != null) {

					Iterator<Entry<String, ArrayList<PluginIntentFilter>>> entry = intentFilter.entrySet().iterator();
					while (entry.hasNext()) {
						Entry<String, ArrayList<PluginIntentFilter>> item = entry.next();
						Iterator<PluginIntentFilter> values = item.getValue().iterator();
						while (values.hasNext()) {
							PluginIntentFilter filter = values.next();
							int result = filter.match(intent.getAction(), intent.getType(), intent.getScheme(),
									intent.getData(), intent.getCategories());

							LogUtil.d("PluginLoader", "result ", result, filter.getAction(0), intent.getAction(),
									intent.getType(), intent.getScheme(), intent.getData());

							if (result != PluginIntentFilter.NO_MATCH_ACTION
									&& result != PluginIntentFilter.NO_MATCH_CATEGORY
									&& result != PluginIntentFilter.NO_MATCH_DATA
									&& result != PluginIntentFilter.NO_MATCH_TYPE) {
								LogUtil.d("PluginLoader", "通过IntentFilter进行匹配的成功");
								return item.getKey();
							}
						}
					}
				}

			}

		}
		return null;
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
