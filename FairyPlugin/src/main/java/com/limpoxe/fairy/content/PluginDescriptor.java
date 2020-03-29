package com.limpoxe.fairy.content;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.util.LogUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * <Pre>
 * @author cailiming
 * </Pre>
 */
public class PluginDescriptor implements Serializable {

	public static final int UNKOWN = 0;
	public static final int BROADCAST = 1;
	public static final int ACTIVITY = 2;
	public static final int SERVICE = 4;
	public static final int PROVIDER = 6;
	public static final int FRAGMENT = 8;
	public static final int FUNCTION = 9;

    private static final long serialVersionUID = 6742761531732381741L;

    private String packageName;

	private String platformBuildVersionCode;

	private String platformBuildVersionName;

	private String minSdkVersion;

	private String targetSdkVersion;

	private String versionCode;
	private String versionName;

	private String requiredHostVersionName;

	private boolean autoStart;

	private String description;

	private boolean isStandalone;

	private boolean isEnabled;

	private String applicationName;

	private int applicationIcon;

	private int applicationLogo;

	private int applicationTheme;

    private boolean useHostPackageName;

	/**
	 * 定义在插件Manifest中的meta-data标签
	 */

	private transient Bundle metaData;

	private HashMap<String, String> metaDataString = new HashMap<String, String>();
	private HashMap<String, Integer> metaDataResource = new HashMap<String, Integer>();
	private HashMap<String, String> metaDataTobeInflate = new HashMap<String, String>();

	private HashMap<String, PluginProviderInfo> providerInfos = new HashMap<String, PluginProviderInfo>();

	/**
	 * key: fragment id,
	 * value: fragment class
	 */
	private HashMap<String, String> fragments = new HashMap<String, String>();

	/**
	 * key: localservice id,
	 * value: localservice class
	 */
	private HashMap<String, String> functions = new HashMap<String, String>();

	/**
	 * key: activity class name
	 * value: intentfilter list
	 */
	private HashMap<String, ArrayList<PluginIntentFilter>> activitys = new HashMap<String, ArrayList<PluginIntentFilter>>();

	/**
	 * key: activity class name
	 * value: activity info in Manifest
	 */
	private HashMap<String, PluginActivityInfo> activityInfos = new HashMap<String, PluginActivityInfo>();

	/**
	 * key: service class name
	 * value: intentfilter list
	 */
	private HashMap<String, ArrayList<PluginIntentFilter>> services = new HashMap<String, ArrayList<PluginIntentFilter>>();

	private HashMap<String, String> serviceInfos = new HashMap<String, String>();

	/**
	 * key: receiver class name
	 * value: intentfilter list
	 */
	private HashMap<String, ArrayList<PluginIntentFilter>> receivers = new HashMap<String, ArrayList<PluginIntentFilter>>();

	private String rootDir;
	private String versionedRootDir;
	private String nativeLibDir;
	private String dalvikCacheDir;
	private String installedPath;

	private long installationTime;

	private String[] dependencies;

	private ArrayList<String> muliDexList;

	private transient HashMap<Integer, PackageInfo> packageInfoHashMap;

	//=============getter and setter======================

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPlatformBuildVersionCode() {
		return platformBuildVersionCode;
	}

	public void setPlatformBuildVersionCode(String platformBuildVersionCode) {
		this.platformBuildVersionCode = platformBuildVersionCode;
	}

	public String getPlatformBuildVersionName() {
		return platformBuildVersionName;
	}

	public void setPlatformBuildVersionName(String platformBuildVersionName) {
		this.platformBuildVersionName = platformBuildVersionName;
	}

	public String getMinSdkVersion() {
		return minSdkVersion;
	}

	public void setMinSdkVersion(String minSdkVersion) {
		this.minSdkVersion = minSdkVersion;
	}

	public String getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public String getVersion() {
		return versionName + "_" + versionCode;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

    public void setRequiredHostVersionName(String requiredHostVersionName) {
        this.requiredHostVersionName = requiredHostVersionName;
    }

    public String getRequiredHostVersionName() {
        return requiredHostVersionName;
    }

	public boolean getAutoStart() {
		return autoStart;
	}

	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getApplicationIcon() {
		return applicationIcon;
	}

	public void setApplicationIcon(int icon) {
		this.applicationIcon = icon;
	}

	public int getApplicationLogo() {
		return applicationLogo;
	}

	public void setApplicationLogo(int logo) {
		this.applicationLogo = logo;
	}

	public int getApplicationTheme() {
		return applicationTheme;
	}

	public void setApplicationTheme(int theme) {
		this.applicationTheme = theme;
	}

	public static void inflateMetaData(PluginDescriptor descriptor, Resources pluginRes) {
		Bundle metaData = descriptor.getMetaData();


		if (metaData == null) {
			LogUtil.i("开始填充插件MetaData");

			metaData = new Bundle();
			descriptor.setMetaData(metaData);

			Iterator<Map.Entry<String, String>> strItr = descriptor.getMetaDataString().entrySet().iterator();
			while(strItr.hasNext()) {
				Map.Entry<String, String> entry = strItr.next();
				LogUtil.d(entry.getKey(), entry.getValue());
				metaData.putString(entry.getKey(), entry.getValue());
			}

			Iterator<Map.Entry<String, Integer>> resItr = descriptor.getMetaDataResource().entrySet().iterator();
			while(resItr.hasNext()) {
				Map.Entry<String, Integer> entry = resItr.next();
				LogUtil.d(entry.getKey(), entry.getValue());
				metaData.putInt(entry.getKey(), entry.getValue());
			}

			HashMap<String, String> resIdData = descriptor.getMetaDataTobeInflate();
			if (resIdData != null) {
				Iterator<Map.Entry<String, String>> itr = resIdData.entrySet().iterator();
				while(itr.hasNext()){
					Map.Entry<String, String> entry = itr.next();
					String resId = entry.getValue();
					String key = entry.getKey();

					String packageName = null;
					int id = 0;
					if (resId.contains(":")) {
						String[] names = resId.split(":");
						packageName = names[0].replace("@", "");
						id = (int)Long.parseLong(names[1], 16);
					} else {
						packageName = descriptor.getPackageName();
						id = (int)Long.parseLong(resId.replace("@", ""), 16);
					}

					Resources resources = null;
					if (packageName.equals(descriptor.getPackageName())) {
						resources = pluginRes;
					} else if (packageName.equals(FairyGlobal.getHostApplication().getPackageName())) {
						resources = FairyGlobal.getHostApplication().getResources();
					} else if (packageName.equals("android")) {
						resources = Resources.getSystem();
					} else {
						//??
					}

					if (resources != null && id != 0) {
						String type = resources.getResourceTypeName(id);
						LogUtil.d("inflateMetaData", "type", type, id, key);
						if ("string".equals(type)) {
							metaData.putString(key, resources.getString(id));
						} else if ("integer".equals(type)) {
							metaData.getInt(key, resources.getInteger(id));
						} else if ("boolean".equals(type)) {
							metaData.putBoolean(key, resources.getBoolean(id));
						} else {
							//int array??
						}
					}
				}
			}
			LogUtil.i("填充插件MetaData 完成");
		}
	}

	public Bundle getMetaData() {
		return metaData;
	}

	public void setMetaData(Bundle metaData) {
		this.metaData = metaData;
	}

	public HashMap<String, String> getMetaDataString() {
		return metaDataString;
	}

	public void setMetaDataString(HashMap<String, String> metaDataString) {
		this.metaDataString = metaDataString;
	}

	public HashMap<String, Integer> getMetaDataResource() {
		return metaDataResource;
	}

	public void setMetaDataResource(HashMap<String, Integer> metaDataResource) {
		this.metaDataResource = metaDataResource;
	}

	public HashMap<String, String> getMetaDataTobeInflate() {
		return metaDataTobeInflate;
	}

	public void setMetaDataTobeInflate(HashMap<String, String> metaDataTobeInflate) {
		this.metaDataTobeInflate = metaDataTobeInflate;
	}


	public HashMap<String, String> getFragments() {
		return fragments;
	}

	public void setfragments(HashMap<String, String> fragments) {
		this.fragments = fragments;
	}

	public HashMap<String, String> getFunctions() {
		return functions;
	}

	public void setFunctions(HashMap<String, String> functions) {
		this.functions = functions;
	}

	public HashMap<String, ArrayList<PluginIntentFilter>> getReceivers() {
		return receivers;
	}

	public void setReceivers(HashMap<String, ArrayList<PluginIntentFilter>> receivers) {
		this.receivers = receivers;
	}

	public HashMap<String, ArrayList<PluginIntentFilter>> getActivitys() {
		return activitys;
	}

	public void setActivitys(HashMap<String, ArrayList<PluginIntentFilter>> activitys) {
		this.activitys = activitys;
	}

	public HashMap<String, PluginActivityInfo> getActivityInfos() {
		return activityInfos;
	}

	public void setActivityInfos(HashMap<String, PluginActivityInfo> activityInfos) {
		this.activityInfos = activityInfos;
	}

	public HashMap<String, String> getServiceInfos() {
		return serviceInfos;
	}

	public void setServiceInfos(HashMap<String, String> serviceInfos) {
		this.serviceInfos = serviceInfos;
	}

	public HashMap<String, ArrayList<PluginIntentFilter>> getServices() {
		return services;
	}

	public void setServices(HashMap<String, ArrayList<PluginIntentFilter>> services) {
		this.services = services;
	}

	public String getRootDir() {
		if (rootDir == null) {
			if (installedPath != null) {
				// 使用installedPath获取rootDir是为了数据结构兼容
				rootDir = new File(getVersionedRootDir()).getParentFile().getAbsolutePath();
			}
		}
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

	public String getVersionedRootDir() {
		if (versionedRootDir == null) {
			if (installedPath != null) {
				// 使用installedPath获取rootDir是为了数据结构兼容
				versionedRootDir = new File(installedPath).getParentFile().getAbsolutePath();
			}
		}
		return versionedRootDir;
	}

	public void setVersionedRootDir(String versionedRootDir) {
		this.versionedRootDir = versionedRootDir;
	}

	public String getNativeLibDir() {
		if (nativeLibDir == null) {
			if (installedPath != null) {
				// 使用installedPath获取rootDir是为了数据结构兼容
				nativeLibDir = getVersionedRootDir() + File.separator + "lib";
			}
		}
		return nativeLibDir;
	}

	public void setNativeLibDir(String nativeLibDir) {
		this.nativeLibDir = nativeLibDir;
	}

	public String getDalvikCacheDir() {
		if (dalvikCacheDir == null) {
			if (installedPath != null) {
				// 使用installedPath获取rootDir是为了数据结构兼容
				dalvikCacheDir = getVersionedRootDir() + File.separator + "dalvik-cache";
			}
		}
		return dalvikCacheDir;
	}

	public void setDalvikCacheDir(String dalvikCacheDir) {
		this.dalvikCacheDir = dalvikCacheDir;
	}

	public String getInstalledPath() {
		return installedPath;
	}

	public void setInstalledPath(String installedPath) {
		this.installedPath = installedPath;
	}

	public String[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(String[] dependencies) {
		this.dependencies = dependencies;
	}

	public List<String> getMuliDexList() {
		return muliDexList;
	}

	public void setMuliDexList(ArrayList<String> muliDexList) {
		this.muliDexList = muliDexList;
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

	public boolean isStandalone() {
		return isStandalone;
	}

	public void setStandalone(boolean isStandalone) {
		this.isStandalone = isStandalone;
	}

	public HashMap<String, PluginProviderInfo> getProviderInfos() {
		return providerInfos;
	}

	public void setProviderInfos(HashMap<String, PluginProviderInfo> providerInfos) {
		this.providerInfos = providerInfos;
	}

    public boolean isUseHostPackageName() {
        return useHostPackageName;
    }

    public void setUseHostPackageName(boolean useHostPackageName) {
        this.useHostPackageName = useHostPackageName;
    }

	public long getInstallationTime() {
		return installationTime;
	}

	public void setInstallationTime(long installationTime) {
		this.installationTime = installationTime;
	}

	/**
	 * 需要根据id查询的只有fragment
	 * @param clazzId
	 * @return
	 */
	public String getPluginClassNameById(String clazzId) {
		String clazzName = getFragments().get(clazzId);

		if (clazzName == null) {
			LogUtil.w("PluginDescriptor", "clazzName not found for classId ", clazzId);
		} else {
			LogUtil.v("PluginDescriptor", "clazzName found ", clazzName);
		}
		return clazzName;
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
		} else if (getActivitys().containsKey(clazzName) && isEnabled()) {
			return true;
		}  else if (getReceivers().containsKey(clazzName) && isEnabled()) {
			return true;
		}  else if (getServices().containsKey(clazzName) && isEnabled()) {
			return true;
		} else if (getProviderInfos().containsKey(clazzName) && isEnabled()) {
			return true;
		} else if (getApplicationName().equals(clazzName) && !clazzName.equals(Application.class.getName()) && isEnabled()) {
			return true;
		}
		return false;
	}

	/**
	 * 获取class的类型： activity
	 * @return
	 */
	public int getType(String clazzName) {
		if (getFragments().containsValue(clazzName) && isEnabled()) {
			return FRAGMENT;
		} else if (getActivitys().containsKey(clazzName) && isEnabled()) {
			return ACTIVITY;
		}  else if (getReceivers().containsKey(clazzName) && isEnabled()) {
			return BROADCAST;
		}  else if (getServices().containsKey(clazzName) && isEnabled()) {
			return SERVICE;
		} else if (getProviderInfos().containsKey(clazzName) && isEnabled()) {
			return PROVIDER;
		}
		return UNKOWN;
	}

	public ArrayList<String> matchPlugin(Intent intent, int type) {
		PluginDescriptor plugin = this;
        ArrayList<String> result = null;
		String clazzName = null;
		// 如果是通过组件进行匹配的, 这里忽略了packageName
		if (intent.getComponent() != null) {
			if (plugin.containsName(intent.getComponent().getClassName())) {
				clazzName = intent.getComponent().getClassName();
				result = new ArrayList<String>(1);
				result.add(clazzName);
                LogUtil.e("暂时不考虑不同的插件中配置了相同类全名的组件的问题, 先到先得", getPackageName(), clazzName);
				return result;
			}
		} else {
			// 如果是通过IntentFilter进行匹配的
            ArrayList<String> list = null;
			if (type == PluginDescriptor.ACTIVITY) {
				list  = findClassNameByIntent(intent, plugin.getActivitys());
			} else if (type == PluginDescriptor.SERVICE) {
				list  = findClassNameByIntent(intent, plugin.getServices());
			} else if (type == PluginDescriptor.BROADCAST) {
				list  = findClassNameByIntent(intent, plugin.getReceivers());
			}
			return list;
		}
		return null;
	}

	private static ArrayList<String> findClassNameByIntent(Intent intent, HashMap<String, ArrayList<PluginIntentFilter>> intentFilter) {
		if (intentFilter != null) {
			ArrayList<String> targetClassNameList = null;

			Iterator<Map.Entry<String, ArrayList<PluginIntentFilter>>> entry = intentFilter.entrySet().iterator();
			while (entry.hasNext()) {
				Map.Entry<String, ArrayList<PluginIntentFilter>> item = entry.next();
				Iterator<PluginIntentFilter> values = item.getValue().iterator();
				while (values.hasNext()) {
					PluginIntentFilter filter = values.next();
					int result = filter.match(intent.getAction(), intent.getType(), intent.getScheme(),
							intent.getData(), intent.getCategories());

					if (result != PluginIntentFilter.NO_MATCH_ACTION
							&& result != PluginIntentFilter.NO_MATCH_CATEGORY
							&& result != PluginIntentFilter.NO_MATCH_DATA
							&& result != PluginIntentFilter.NO_MATCH_TYPE) {
						if (targetClassNameList == null) {
							targetClassNameList = new ArrayList<String>();
						}
						targetClassNameList.add(item.getKey());
					}
				}
			}
			return targetClassNameList;
		}
		return null;
	}

	public PackageInfo getPackageInfo(Integer flags) {
		if (packageInfoHashMap == null) {
			packageInfoHashMap = new HashMap<>();
		}
		PackageInfo packageInfo = packageInfoHashMap.get(flags);
		if (packageInfo == null) {
			packageInfo = FairyGlobal.getHostApplication().getPackageManager().getPackageArchiveInfo(getInstalledPath(), flags);
			if (packageInfo != null && packageInfo.applicationInfo != null) {
				packageInfo.applicationInfo.sourceDir = getInstalledPath();
				packageInfo.applicationInfo.publicSourceDir = getInstalledPath();
			}
			packageInfoHashMap.put(flags, packageInfo);
		}

		return packageInfo;
	}
}
