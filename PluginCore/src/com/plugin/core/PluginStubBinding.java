package com.plugin.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Base64;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 插件组件动态绑定到宿主的虚拟stub组件
 */
public class PluginStubBinding {

	private static final String STUB_DEFAULT = "com.plugin.core.STUB_DEFAULT";
	private static final String STUB_EXACT = "com.plugin.core.STUB_EXACT";

	/**
	 * key:stub Activity Name
	 * value:plugin Activity Name
	 */
	private static HashMap<String, String> singleTaskActivityMapping = new HashMap<String, String>();
	private static HashMap<String, String> singleTopActivityMapping = new HashMap<String, String>();
	private static HashMap<String, String> singleInstanceActivityMapping = new HashMap<String, String>();
	private static String standardActivity = null;
	private static String receiver = null;
	/**
	 * key:stub Service Name
	 * value:plugin Service Name
	 */
	private static HashMap<String, String> serviceMapping = new HashMap<String, String>();

	private static Set<String> mExcatStubSet;

	private static boolean isPoolInited = false;

	private static void initPool() {
		if (isPoolInited) {
			return;
		}

		loadStubActivity();

		loadStubService();

		loadStubExactly();

		loadStubReceiver();

		isPoolInited = true;
	}

	private static void loadStubActivity() {
		Intent launchModeIntent = new Intent();
		launchModeIntent.setAction(STUB_DEFAULT);
		launchModeIntent.setPackage(PluginLoader.getApplicatoin().getPackageName());

		List<ResolveInfo> list = PluginLoader.getApplicatoin().getPackageManager().queryIntentActivities(launchModeIntent, PackageManager.MATCH_DEFAULT_ONLY);

		if (list != null && list.size() >0) {
			for (ResolveInfo resolveInfo:
					list) {
				if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

					singleTaskActivityMapping.put(resolveInfo.activityInfo.name, null);

				} else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

					singleTopActivityMapping.put(resolveInfo.activityInfo.name, null);

				} else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

					singleInstanceActivityMapping.put(resolveInfo.activityInfo.name, null);

				} else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {

					standardActivity = resolveInfo.activityInfo.name;

				}

			}
		}

	}

	private static void loadStubService() {
		Intent launchModeIntent = new Intent();
		launchModeIntent.setAction(STUB_DEFAULT);
		launchModeIntent.setPackage(PluginLoader.getApplicatoin().getPackageName());

		List<ResolveInfo> list = PluginLoader.getApplicatoin().getPackageManager().queryIntentServices(launchModeIntent, PackageManager.MATCH_DEFAULT_ONLY);

		if (list != null && list.size() >0) {
			for (ResolveInfo resolveInfo:
					list) {
				serviceMapping.put(resolveInfo.serviceInfo.name, null);
			}
			HashMap<String, String> mapping = restore();
			if (mapping != null) {
				serviceMapping.putAll(mapping);
			}
			//只有service需要固化
			save(serviceMapping);
		}
	}

	private static void loadStubExactly() {
		Intent exactStub = new Intent();
		exactStub.setAction(STUB_EXACT);
		exactStub.setPackage(PluginLoader.getApplicatoin().getPackageName());

		//精确匹配的activity
		List<ResolveInfo> resolveInfos = PluginLoader.getApplicatoin().getPackageManager().queryIntentActivities(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfos != null && resolveInfos.size() > 0) {
			if (mExcatStubSet == null) {
				mExcatStubSet = new HashSet<String>();
			}
			for(ResolveInfo info:resolveInfos) {
				mExcatStubSet.add(info.activityInfo.name);
			}
		}

		//精确匹配的service
		resolveInfos = PluginLoader.getApplicatoin().getPackageManager().queryIntentServices(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfos != null && resolveInfos.size() > 0) {
			if (mExcatStubSet == null) {
				mExcatStubSet = new HashSet<String>();
			}
			for(ResolveInfo info:resolveInfos) {
				mExcatStubSet.add(info.serviceInfo.name);
			}
		}

	}

	private static void loadStubReceiver() {
		Intent exactStub = new Intent();
		exactStub.setAction(STUB_DEFAULT);
		exactStub.setPackage(PluginLoader.getApplicatoin().getPackageName());

		List<ResolveInfo> resolveInfos = PluginLoader.getApplicatoin().getPackageManager().queryBroadcastReceivers(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfos != null && resolveInfos.size() >0) {
			receiver = resolveInfos.get(0).activityInfo.name;
		}

	}

	public static String bindStubReceiver() {
		initPool();
		return receiver;
	}

	public static String bindStubActivity(String pluginActivityClassName, int launchMode) {

		initPool();

		if (isExact(pluginActivityClassName, PluginDescriptor.ACTIVITY)) {
			return pluginActivityClassName;
		}


		HashMap<String, String> bindingMapping = null;

		if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {

			return standardActivity;

		} else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

			bindingMapping = singleTaskActivityMapping;

		} else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

			bindingMapping = singleTopActivityMapping;

		} else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

			bindingMapping = singleInstanceActivityMapping;

		}

		if (bindingMapping != null) {

			Iterator<Map.Entry<String, String>> itr = bindingMapping.entrySet().iterator();
			String idleStubActivityName = null;

			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				if (entry.getValue() == null) {
					if (idleStubActivityName == null) {
						idleStubActivityName = entry.getKey();
						//这里找到空闲的stubactivity以后，还需继续遍历，用来检查是否pluginActivityClassName已经绑定过了
					}
				} else if (pluginActivityClassName.equals(entry.getValue())) {
					//已绑定过，直接返回
					return entry.getKey();
				}
			}

			//没有绑定到StubActivity，而且还有空余的stubActivity，进行绑定
			if (idleStubActivityName != null) {
				bindingMapping.put(idleStubActivityName, pluginActivityClassName);
				return idleStubActivityName;
			}

		}

		return standardActivity;
	}

	private static boolean isExact(String name, int type) {
		initPool();

		if (mExcatStubSet != null && mExcatStubSet.size() > 0) {
			return mExcatStubSet.contains(name);
		}

		return false;
	}

	public static void unBindLaunchModeStubActivity(String activityName, Intent intent) {
		if (intent != null) {
			ComponentName cn = intent.getComponent();
			if (cn != null) {
				String pluginActivityName = cn.getClassName();

				if (pluginActivityName.equals(singleTaskActivityMapping.get(activityName))) {

					singleTaskActivityMapping.put(activityName, null);

				} else if (pluginActivityName.equals(singleInstanceActivityMapping.get(activityName))) {

					singleInstanceActivityMapping.put(activityName, null);

				} else {
					//对于standard和singleTop的launchmode，不做处理。
				}
			}
		}
	}

	public static String getBindedPluginServiceName(String stubServiceName) {

		initPool();

		if (isExact(stubServiceName, PluginDescriptor.SERVICE)) {
			return stubServiceName;
		}

		Iterator<Map.Entry<String, String>> itr = serviceMapping.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();

			if (entry.getKey().equals(stubServiceName)) {
				return entry.getValue();
			}
		}

		//没有找到，尝试重磁盘恢复
		HashMap<String, String> mapping = restore();
		if (mapping != null) {
			itr = mapping.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();

				if (entry.getKey().equals(stubServiceName)) {
					serviceMapping.put(stubServiceName, entry.getValue());
					save(serviceMapping);
					return entry.getValue();
				}
			}
		}

		return null;
	}

	public static String bindStubService(String pluginServiceClassName) {

		initPool();

		if (isExact(pluginServiceClassName, PluginDescriptor.SERVICE)) {
			return pluginServiceClassName;
		}

		Iterator<Map.Entry<String, String>> itr = serviceMapping.entrySet().iterator();

		String idleStubServiceName = null;

		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			if (entry.getValue() == null) {
				if (idleStubServiceName == null) {
					idleStubServiceName = entry.getKey();
					//这里找到空闲的idleStubServiceName以后，还需继续遍历，用来检查是否pluginServiceClassName已经绑定过了
				}
			} else if (pluginServiceClassName.equals(entry.getValue())) {
				//已经绑定过，直接返回
				LogUtil.d("已经绑定过", entry.getKey(), pluginServiceClassName);
				return entry.getKey();
			}
		}

		//没有绑定到StubService，而且还有空余的StubService，进行绑定
		if (idleStubServiceName != null) {
			LogUtil.d("添加绑定", idleStubServiceName, pluginServiceClassName);
			serviceMapping.put(idleStubServiceName, pluginServiceClassName);
			//对serviceMapping持久化是因为如果service处于运行状态时app发生了crash，系统会自动恢复之前的service，此时插件映射信息查不到的话会再次crash
			save(serviceMapping);
			return idleStubServiceName;
		}

		//绑定失败
		return null;
	}

	public static void unBindStubService(String pluginServiceName) {
		Iterator<Map.Entry<String, String>> itr = serviceMapping.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			if (pluginServiceName.equals(entry.getValue())) {
				//如果存在绑定关系，解绑
				LogUtil.d("回收绑定", entry.getKey(), entry.getValue());
				serviceMapping.put(entry.getKey(), null);
				save(serviceMapping);
				break;
			}
		}
	}

	private static boolean save(HashMap<String, String> mapping) {

		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(mapping);
			objectOutputStream.flush();

			byte[] data = byteArrayOutputStream.toByteArray();
			String list = Base64.encodeToString(data, Base64.DEFAULT);

			PluginLoader.getApplicatoin()
					.getSharedPreferences("plugins.serviceMapping", Context.MODE_PRIVATE)
					.edit().putString("plugins.serviceMapping.map", list).commit();

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

	private static HashMap<String, String> restore() {
		String list = PluginLoader.getApplicatoin()
				.getSharedPreferences("plugins.serviceMapping", Context.MODE_PRIVATE)
				.getString("plugins.serviceMapping.map", "");
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

			HashMap<String, String> mapping = (HashMap<String, String>) object;
			return mapping;
		}
		return null;
	}

	public static boolean isStubActivity(String className) {
		initPool();

		return isExact(className, PluginDescriptor.ACTIVITY) || className.equals(standardActivity) || singleTaskActivityMapping.containsKey(className)
				|| singleTopActivityMapping.containsKey(className)
				|| singleInstanceActivityMapping.containsKey(className);
	}
}
