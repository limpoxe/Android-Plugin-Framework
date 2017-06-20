package com.limpoxe.fairy.content;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

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

	private static final long serialVersionUID = -7545734825911798344L;

	public static final int UNKOWN = 0;
	public static final int BROADCAST = 1;
	public static final int ACTIVITY = 2;
	public static final int SERVICE = 4;
	public static final int PROVIDER = 6;
	public static final int FRAGMENT = 8;
	public static final int FUNCTION = 9;

	private String packageName;

	private String platformBuildVersionCode;

	private String platformBuildVersionName;

	private String minSdkVersion;

	private String targetSdkVersion;

	private String version;

    private String requiredHostVersionName;

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

	private String installedPath;

	private String[] dependencies;

	private ArrayList<String> muliDexList;

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
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

    public void setRequiredHostVersionName(String requiredHostVersionName) {
        this.requiredHostVersionName = requiredHostVersionName;
    }

    public String getRequiredHostVersionName() {
        return requiredHostVersionName;
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

	public Bundle getMetaData() {
		if (metaData == null) {
			if (installedPath != null) {
				metaData = ResourceUtil.getApplicationMetaData(installedPath);
				if (metaData == null) {
					metaData = new Bundle();
				}
			}
		}
		return metaData;
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
                LogUtil.e("暂时不考虑不同的插件中配置了相同类全名的组件的问题, 先到先得");
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
}
