package com.plugin.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.content.PluginProviderInfo;
import com.plugin.core.PluginLoader;

public class ManifestParser {

	public static PluginDescriptor parseManifest(String pluginPath) {
    	
        try {
        	ZipFile zipFile = new ZipFile(new File(pluginPath), ZipFile.OPEN_READ);
            ZipEntry manifestXmlEntry = zipFile.getEntry(ManifestReader.DEFAULT_XML);
        	String manifestXml = ManifestReader.getManifestXMLFromAPK(zipFile, manifestXmlEntry);
        	
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(manifestXml));
            int eventType = parser.getEventType();
            String namespaceAndroid = null;
            String packageName = null;
            
            PluginDescriptor desciptor = new PluginDescriptor();
            do {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: {
                        break;
                    }
                    case XmlPullParser.START_TAG: {
                        String tag = parser.getName();
                        if (tag.equals("manifest")) {
                        	
                            namespaceAndroid = parser.getNamespace("android");
                            
                            packageName = parser.getAttributeValue(null, "package");
                            String versionCode = parser.getAttributeValue(namespaceAndroid, "versionCode");
                            String versionName = parser.getAttributeValue(namespaceAndroid, "versionName");
                           
                            //用这个字段来标记apk是独立apk，还是需要依赖主程序的class和resource
                            //当这个值等于宿主程序packageName时，则认为这个插件是需要依赖宿主的class和resource的
                            String sharedUserId = parser.getAttributeValue(namespaceAndroid, "sharedUserId");
                           
                            desciptor.setPackageName(packageName);
                            desciptor.setVersion(versionName + "_" + versionCode);
                            
                            desciptor.setStandalone(sharedUserId==null || !PluginLoader.getApplicatoin().getPackageName().equals(sharedUserId));
                            
                            LogUtil.d(packageName, versionCode, versionName, sharedUserId);
                        } else if (tag.equals("meta-data")) {

                        	HashMap<String, String> hashMap = desciptor.getFragments();
                        	if (hashMap == null) {
                        		hashMap = new HashMap<String, String>();
                        		desciptor.setfragments(hashMap);
                        	}
                        	
                        	String fragmentId = parser.getAttributeValue(namespaceAndroid, "name");
                        	String fragmentClassName = parser.getAttributeValue(namespaceAndroid, "value");

                            /**
                             * fragmentId 约定以fragment_id_开头，避免把其他普通metadata信息读出来了
                             */
                            if (fragmentId != null && fragmentId.startsWith("fragment_id_")) {
                                hashMap.put(fragmentId, fragmentClassName);
                                LogUtil.d(fragmentId, fragmentClassName);
                            }

                        } else if ("application".equals(parser.getName())) {
                        	
                        	String applicationName = parser.getAttributeValue(namespaceAndroid, "name");
                    		if (applicationName != null) {
                    			applicationName = getName(applicationName, packageName);
                    			desciptor.setApplicationName(applicationName);
                    		}
                    		desciptor.setDescription(parser.getAttributeValue(namespaceAndroid, "label"));
                    		
                    		LogUtil.d(" applicationName " + applicationName + " Description " + desciptor.getDescription());

                        } else if ("activity".equals(parser.getName())) {

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getActivitys();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setActivitys(map);
                            }
                        	addIntentFilter(map, packageName, namespaceAndroid, parser, "activity");

                        } else if ("receiver".equals(parser.getName())) {

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getReceivers();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setReceivers(map);
                            }
                        	addIntentFilter(map, packageName, namespaceAndroid, parser, "receiver");

                        } else if ("service".equals(parser.getName())) {

                            HashMap<String, ArrayList<PluginIntentFilter>> map = desciptor.getServices();
                            if (map == null) {
                                map = new HashMap<String, ArrayList<PluginIntentFilter>>();
                                desciptor.setServices(map);
                            }
                        	addIntentFilter(map, packageName, namespaceAndroid, parser, "service");

                        } else if ("provider".equals(parser.getName())) {

                            String name = parser.getAttributeValue(namespaceAndroid, "name");
                            String author = parser.getAttributeValue(namespaceAndroid, "authorities");
                            String exported = parser.getAttributeValue(namespaceAndroid, "exported");
                            HashMap<String, PluginProviderInfo> providers = desciptor.getProviderInfos();
                            if (providers == null) {
                                providers = new HashMap<String, PluginProviderInfo>();
                                desciptor.setProviderInfos(providers);
                            }

                            PluginProviderInfo info = new PluginProviderInfo();
                            info.setName(PluginProviderInfo.prefix + name);//name做上标记，表示是来自插件，方便classloader进行判断
                            info.setExported(Boolean.getBoolean(exported));
                            info.setAuthority(author);

                            providers.put(name, info);
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
            
            return desciptor;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
	private static void addIntentFilter(HashMap<String, ArrayList<PluginIntentFilter>> map, String packageName, String namespace,
			XmlPullParser parser, String endTagName) throws XmlPullParserException, IOException {
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
						intentFilter.readFromXml(tag, parser);
					}
				}
			}
			eventType = parser.next();
		} while (!endTagName.equals(parser.getName()));//再次到达，表示一个标签结束了

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
