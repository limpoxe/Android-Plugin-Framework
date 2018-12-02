package com.limpoxe.fairy.manager;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.limpoxe.fairy.content.PluginActivityInfo;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.content.PluginIntentFilter;
import com.limpoxe.fairy.content.PluginProviderInfo;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackAssetManager;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PluginManifestParser {

    public static PluginDescriptor parseManifest(String pluginPath) {
        try {

            AssetManager assetManager = AssetManager.class.newInstance();
            new HackAssetManager(assetManager).addAssetPath(pluginPath);
            XmlResourceParser parser = assetManager.openXmlResourceParser("AndroidManifest.xml");

            int eventType = parser.getEventType();
            String namespaceAndroid = null;
            String packageName = null;

            ArrayList<String> dependencies = null;

            PluginDescriptor desciptor = new PluginDescriptor();
            do {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: {
                        break;
                    }
                    case XmlPullParser.START_TAG: {
                        String tag = parser.getName();
                        if ("manifest".equals(tag)) {

                            namespaceAndroid = parser.getAttributeNamespace(0);
                            if (TextUtils.isEmpty(namespaceAndroid)) {
                                namespaceAndroid = "http://schemas.android.com/apk/res/android";
                            }

                            packageName = parser.getAttributeValue(null, "package");
                            String useHostPackageName = parser.getAttributeValue(null, "useHostPackageName");
                            String versionCode = parser.getAttributeValue(namespaceAndroid, "versionCode");
                            String versionName = parser.getAttributeValue(namespaceAndroid, "versionName");
                            String platformBuildVersionCode = parser.getAttributeValue(null, "platformBuildVersionCode");
                            String platformBuildVersionName = parser.getAttributeValue(null, "platformBuildVersionName");

                            //用这个字段来标记apk是独立apk，还是需要依赖主程序的class和resource
                            //当这个值等于宿主程序packageName时，则认为这个插件是需要依赖宿主的class和resource的
                            String hostApplicationId = parser.getAttributeValue(null, "hostApplicationId");
                            if (hostApplicationId == null) {
                                hostApplicationId = parser.getAttributeValue(namespaceAndroid, "sharedUserId");
                            }

                            //这个字段用来标记非独立插件以来的宿主版本号，即此当前插件仅可运行在此版本的宿主中
                            //独立插件忽略此项
                            String requiredHostVersionName = parser.getAttributeValue(null, "requiredHostVersionName");
                            String requiredHostVersionCode = parser.getAttributeValue(null, "requiredHostVersionCode");

                            String autoStart = parser.getAttributeValue(null, "autoStart");

                            desciptor.setPackageName(packageName);
                            desciptor.setVersionName(versionName);
                            desciptor.setVersionCode(versionCode);
                            desciptor.setPlatformBuildVersionCode(platformBuildVersionCode);
                            desciptor.setPlatformBuildVersionName(platformBuildVersionName);

                            desciptor.setStandalone(hostApplicationId == null || !FairyGlobal.getHostApplication().getPackageName().equals(hostApplicationId));
                            if (!desciptor.isStandalone() && !TextUtils.isEmpty(requiredHostVersionName)) {
                                desciptor.setRequiredHostVersionName(requiredHostVersionName);
                            }

                            desciptor.setUseHostPackageName("true".equals(useHostPackageName));
                            desciptor.setAutoStart("true".equals(autoStart));

                            LogUtil.v(packageName, versionCode, versionName, hostApplicationId, FairyGlobal.getHostApplication().getPackageName());
                        } else if ("uses-sdk".equals(tag))  {

                            String minSdkVersion = parser.getAttributeValue(namespaceAndroid, "minSdkVersion");
                            String targetSdkVersion = parser.getAttributeValue(namespaceAndroid, "targetSdkVersion");

                            desciptor.setMinSdkVersion(minSdkVersion);
                            desciptor.setTargetSdkVersion(targetSdkVersion);

                        } else if ("meta-data".equals(tag)) {

                            String type = parser.getAttributeValue(namespaceAndroid, "tag");
                            String name = parser.getAttributeValue(namespaceAndroid, "name");
                        	String value = parser.getAttributeValue(namespaceAndroid, "value");

                            if ("exported-fragment".equals(type)) {

                                value = getName(value, packageName);
                                if (name != null) {

                                    HashMap<String, String> fragments = desciptor.getFragments();
                                    if (fragments == null) {
                                        fragments = new HashMap<String, String>();
                                        desciptor.setfragments(fragments);
                                    }
                                    fragments.put(name, value);
                                    LogUtil.v(name, value);

                                }

                            } else if ("exported-service".equals(type)) {
                                String label = parser.getAttributeValue(namespaceAndroid, "label");

                                value = getName(value, packageName);
                                if (label != null) {
                                    value = value + "|" + label;
                                }
                                if (name != null) {

                                    HashMap<String, String> functions = desciptor.getFunctions();
                                    if (functions == null) {
                                        functions = new HashMap<String, String>();
                                        desciptor.setFunctions(functions);
                                    }
                                    functions.put(name, value);
                                    LogUtil.v(name, value);

                                }

                            } else {
                                if (name != null) {
                                    String resource = parser.getAttributeValue(namespaceAndroid, "resource");
                                    if (value != null && value.startsWith("@")) {
                                        //等插件初始化的时候再处理这类meta信息
                                        desciptor.getMetaDataTobeInflate().put(name, ResourceUtil.covent2Hex(value));
                                    } else if (value != null) {
                                        desciptor.getMetaDataString().put(name, ResourceUtil.covent2Hex(value));
                                    } else if (resource != null && resource.startsWith("@")) {
                                        desciptor.getMetaDataResource().put(name, ResourceUtil.parseResId(ResourceUtil.covent2Hex(resource)));
                                    }

                                    LogUtil.v("meta-data", name, value, resource);
                                }
                            }

                        }  else if ("uses-library".equals(tag)) {

                            String name = parser.getAttributeValue(namespaceAndroid, "name");
                            if (name.startsWith("com.google") || name.startsWith("com.sec.android") || name.startsWith("com.here.android")) {
                                LogUtil.d("uses-library ignore", name);
                            } else {
                                if (dependencies == null) {
                                    dependencies = new ArrayList<String>();
                                }
                                dependencies.add(name);
                            }

                        } else if ("application".equals(tag)) {
                        	
                        	String applicationName = parser.getAttributeValue(namespaceAndroid, "name");
                            if (applicationName == null) {
                                applicationName = Application.class.getName();
                            }
                            applicationName = getName(applicationName, packageName);
                            desciptor.setApplicationName(applicationName);

                    		desciptor.setDescription(ResourceUtil.covent2Hex(parser.getAttributeValue(namespaceAndroid, "label")));

                            //这里不解析主题，后面会通过packageManager查询

                            LogUtil.v("applicationName", applicationName, " Description ", desciptor.getDescription());

                        } else if ("activity".equals(tag)) {

                            String windowSoftInputMode = parser.getAttributeValue(namespaceAndroid, "windowSoftInputMode");//strin
                            String hardwareAccelerated = parser.getAttributeValue(namespaceAndroid, "hardwareAccelerated");//int string
                            String launchMode = parser.getAttributeValue(namespaceAndroid, "launchMode");//string
                            String screenOrientation = parser.getAttributeValue(namespaceAndroid, "screenOrientation");//string
                            String theme = parser.getAttributeValue(namespaceAndroid, "theme");//int
                            String immersive = parser.getAttributeValue(namespaceAndroid, "immersive");//int string
                            String uiOptions = parser.getAttributeValue(namespaceAndroid, "uiOptions");//int string
                            String configChanges = parser.getAttributeValue(namespaceAndroid, "configChanges");//int string
                            String useHostPackageName = parser.getAttributeValue(null, "useHostPackageName");

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getActivitys();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setActivitys(map);
                            }
                            String name = addIntentFilter(map, packageName, namespaceAndroid, parser, "activity");

                            HashMap<String, PluginActivityInfo> infos = desciptor.getActivityInfos();
                            if (infos == null) {
                                infos = new HashMap<String, PluginActivityInfo>();
                                desciptor.setActivityInfos(infos);
                            }

                            PluginActivityInfo pluginActivityInfo = infos.get(name);
                            if (pluginActivityInfo == null) {
                                pluginActivityInfo = new PluginActivityInfo();
                                infos.put(name, pluginActivityInfo);
                            }
                            pluginActivityInfo.setHardwareAccelerated(ResourceUtil.covent2Hex(hardwareAccelerated));
                            pluginActivityInfo.setImmersive(ResourceUtil.covent2Hex(immersive));
                            if (launchMode == null) {
                                launchMode = String.valueOf(ActivityInfo.LAUNCH_MULTIPLE);
                            }
                            pluginActivityInfo.setLaunchMode(launchMode);
                            pluginActivityInfo.setName(name);
                            pluginActivityInfo.setScreenOrientation(screenOrientation);
                            pluginActivityInfo.setTheme(ResourceUtil.covent2Hex(theme));
                            pluginActivityInfo.setWindowSoftInputMode(windowSoftInputMode);
                            pluginActivityInfo.setUiOptions(uiOptions);
                            if (configChanges != null) {
                                pluginActivityInfo.setConfigChanges((int)Long.parseLong(configChanges.replace("0x", ""), 16));
                            }
                            pluginActivityInfo.setUseHostPackageName("true".equals(useHostPackageName));

                        } else if ("receiver".equals(tag)) {

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getReceivers();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setReceivers(map);
                            }
                            addIntentFilter(map, packageName, namespaceAndroid, parser, "receiver");

                        } else if ("service".equals(tag)) {

                            String process = parser.getAttributeValue(namespaceAndroid, "process");

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getServices();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setServices(map);
                            }
                        	String name = addIntentFilter(map, packageName, namespaceAndroid, parser, "service");

                            if (process != null) {
                                HashMap<String, String> infos = desciptor.getServiceInfos();
                                if (infos == null) {
                                    infos = new HashMap<String, String>();
                                    desciptor.setServiceInfos(infos);
                                }
                                infos.put(name, process);
                            }

                        } else if ("provider".equals(tag)) {

                            String name = parser.getAttributeValue(namespaceAndroid, "name");
                            name = getName(name, packageName);
                            String author = parser.getAttributeValue(namespaceAndroid, "authorities");
                            String exported = parser.getAttributeValue(namespaceAndroid, "exported");
                            String grantUriPermissions = parser.getAttributeValue(namespaceAndroid, "grantUriPermissions");
                            HashMap<String, PluginProviderInfo> providers = desciptor.getProviderInfos();
                            if (providers == null) {
                                providers = new HashMap<String, PluginProviderInfo>();
                                desciptor.setProviderInfos(providers);
                            }

                            PluginProviderInfo info = new PluginProviderInfo();
                            info.setName(name);
                            info.setExported("true".equals(exported));
                            info.setAuthority(author);
                            info.setGrantUriPermissions("true".equals(grantUriPermissions));
                            providers.put(name, info);

                            LogUtil.d(name, info.isGrantUriPermissions(), grantUriPermissions, author, exported);
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        break;
                    }
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
            
            desciptor.setEnabled(true);

            //有可能没有配置application节点，这里需要检查一下application
            if (desciptor.getApplicationName() == null) {
                desciptor.setApplicationName(Application.class.getName());
            }

            if (dependencies != null) {
                desciptor.setDependencies((String[])dependencies.toArray(new String[0]));
            }

            return desciptor;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
    
	private static String addIntentFilter(HashMap<String, ArrayList<PluginIntentFilter>> map, String packageName, String namespace,
                                          XmlResourceParser parser, String endTagName) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String activityName = parser.getAttributeValue(namespace, "name");
		activityName = getName(activityName, packageName);

		ArrayList<PluginIntentFilter> filters = map.get(activityName);
		if (filters == null) {
			filters = new ArrayList<PluginIntentFilter>();
            map.put(activityName, filters);
		}
		
		PluginIntentFilter intentFilter = new PluginIntentFilter();
		do {
			switch (eventType) {
				case XmlPullParser.START_TAG: {
					String tag = parser.getName();
					if ("intent-filter".equals(tag)) {
						intentFilter = new PluginIntentFilter();
						filters.add(intentFilter);
					} else {
						intentFilter.readFromXml(tag, namespace, parser);
					}
				}
			}
			eventType = parser.next();
		} while (!endTagName.equals(parser.getName()));//再次到达，表示一个标签结束了

        return activityName;
	}
	
	private static String getName(String nameOrig, String pkgName) {
        if (nameOrig == null) {
            return null;
        }
        StringBuilder sb = null;
        if (nameOrig.startsWith(".")) {
            sb = new StringBuilder();
            sb.append(pkgName);
            sb.append(nameOrig);
        } else if (!nameOrig.contains(".")) {
            sb = new StringBuilder();
            sb.append(pkgName);
            sb.append('.');
            sb.append(nameOrig);
        } else {
            return nameOrig;
        }
        return sb.toString();
    }

}
