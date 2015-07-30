package com.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import com.plugin.core.PluginDescriptor;

public class ApkReader {
	private static final String LOG_TAG = ApkReader.class.getSimpleName();

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
            String versionCode = null;
            String versionName = null;
            
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
                            versionCode = parser.getAttributeValue(namespaceAndroid, "versionCode");
                            versionName = parser.getAttributeValue(namespaceAndroid, "versionName");
                            
                            desciptor.setPackageName(packageName);
                            desciptor.setVersion(versionName + "_" + versionCode);
                            
                            Log.d(LOG_TAG, " " + packageName + " " + versionCode + " " + versionName + " ");
                        } else if (tag.equals("meta-data")) {
                        	HashMap<String, String> hashMap = desciptor.getFragments();
                        	if (hashMap == null) {
                        		hashMap = new HashMap<String, String>();
                        		desciptor.setfragments(hashMap);
                        	}
                        	
                        	String fragmentId = parser.getAttributeValue(namespaceAndroid, "name");
                        	String fragmentClassName = parser.getAttributeValue(namespaceAndroid, "value");
                        	
                        	hashMap.put(fragmentId, fragmentClassName);
                        	
                        	Log.d(LOG_TAG, " " + fragmentId + " " + fragmentClassName);
                        } else if ("application".equals(parser.getName())) {
                        	
                        	String applicationName = parser.getAttributeValue(namespaceAndroid, "name");
                    		if (applicationName != null) {
                    			applicationName = getName(applicationName, packageName);
                    			desciptor.setApplicationName(applicationName);
                    		}
                    		desciptor.setDescription(parser.getAttributeValue(namespaceAndroid, "label"));
                    		
                    		Log.d(LOG_TAG, " applicationName " + applicationName + " " + desciptor.getDescription());
                        } else if ("activity".equals(parser.getName())) {
                        	addIntentFilter(desciptor, packageName, namespaceAndroid, parser, "activity");
                        } else if ("receiver".equals(parser.getName())) {
                        	addIntentFilter(desciptor, packageName, namespaceAndroid, parser, "receiver");
                        } else if ("service".equals(parser.getName())) {
                        	addIntentFilter(desciptor, packageName, namespaceAndroid, parser, "service");
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
    
	private static void addIntentFilter(PluginDescriptor pluginDescription, String packageName, String namespace,
			XmlPullParser parser, String endTagName) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String activityName = parser.getAttributeValue(namespace, "name");
		activityName = getName(activityName, packageName);
		
		HashMap<String, ArrayList<IntentFilter>> activitys = pluginDescription.getComponents();
		if (activitys == null) {
			activitys = new HashMap<String, ArrayList<IntentFilter>>();
			pluginDescription.setComponents(activitys);
		}
		
		ArrayList<IntentFilter> filters = activitys.get(activityName);
		if (filters == null) {
			filters = new ArrayList<IntentFilter>();
			activitys.put(activityName, filters);
		}
		
		IntentFilter intentFilter = new IntentFilter();
		do {
			switch (eventType) {
				case XmlPullParser.START_TAG: {
					String tag = parser.getName();
					if ("intent-filter".equals(tag)) {
						intentFilter = new IntentFilter();
						filters.add(intentFilter);
					} else if ("action".equals(tag)) {
						String actionName = parser.getAttributeValue(namespace,
								"name");
						intentFilter.addAction(actionName);
					} else if ("category".equals(tag)) {
						String category = parser.getAttributeValue(namespace,
								"name");
						intentFilter.addCategory(category);
					} else if ("data".equals(tag)) {
						// TODO parse data
					}
					break;
				}
			}
			eventType = parser.next();
		} while (!endTagName.equals(parser.getName()));//再次到达activity，表示一个activity标签结束了

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
	
//	private static final String PLUGIN_DESCRIPTION_FILE = "assets/plugin.json";

//	public static PluginDescriptor readPluginDescriptor(String pluginFile) {
//
//		Log.d(LOG_TAG, "readPluginDescription:" + pluginFile);
//		String pluginDesciption = ApkReader.readFileFromJar(pluginFile, PLUGIN_DESCRIPTION_FILE);
//		if (!TextUtils.isEmpty(pluginDesciption)) {
//			try {
//				JSONObject json = new JSONObject(pluginDesciption);
//				PluginDescriptor pluginDescriptor = new PluginDescriptor();
//				pluginDescriptor.setEnabled(true);
//				pluginDescriptor.setId(json.get("id").toString());
//				pluginDescriptor.setVersion(json.get("version").toString());
//				pluginDescriptor.setDescription(json.get("description").toString());
//				pluginDescriptor.setApplicationName(json.get("application").toString());
//				
//				HashMap<String, String> pluginFragment = new HashMap<String, String>();
//				JSONObject fragmentJson = json.getJSONObject("fragments");
//				Iterator<String> fragmentIds = fragmentJson.keys();
//				while (fragmentIds.hasNext()) {
//					String fragmenId = fragmentIds.next();
//					pluginFragment.put(fragmenId, fragmentJson.getString(fragmenId));
//				}
//				pluginDescriptor.setfragments(pluginFragment);
//
//				HashMap<String, String> pluginActiviy = new HashMap<String, String>();
//				JSONObject activityJson = json.getJSONObject("activities");
//				Iterator<String> activityIds = activityJson.keys();
//				while (activityIds.hasNext()) {
//					String activityId = activityIds.next();
//					pluginActiviy.put(activityId, activityJson.getString(activityId));
//				}
//				pluginDescriptor.setActivities(pluginActiviy);
//
//				HashMap<String, String> pluginService = new HashMap<String, String>();
//				JSONObject serviceJson = json.getJSONObject("services");
//				Iterator<String> serviceIds = serviceJson.keys();
//				while (serviceIds.hasNext()) {
//					String serviceId = serviceIds.next();
//					pluginService.put(serviceId, serviceJson.getString(serviceId));
//				}
//				pluginDescriptor.setServices(pluginService);
//				
//				return pluginDescriptor;
//			} catch (JSONException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}

	public static boolean copyFile(final InputStream inputStream, String dest) {
		Log.d(LOG_TAG, "copyFile to " + dest);
		FileOutputStream oputStream = null;
		try {
			File destFile = new File(dest);
			destFile.getParentFile().mkdirs();
			destFile.createNewFile();

			oputStream = new FileOutputStream(destFile);
			byte[] bb = new byte[48 * 1024];
			int len = 0;
			while ((len = inputStream.read(bb)) != -1) {
				oputStream.write(bb, 0, len);
			}
			oputStream.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oputStream != null) {
				try {
					oputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean copyFile(String source, String dest) {
		try {
			return copyFile(new FileInputStream(new File(source)), dest);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String readFileFromJar(String jarFilePath, String metaInfo) {
		Log.d(LOG_TAG, "readFileFromJar:" + jarFilePath + "," + metaInfo);
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarFilePath);
			JarEntry entry = jarFile.getJarEntry(metaInfo);
			if (entry != null) {
				InputStream input = jarFile.getInputStream(entry);

				String info = streamToString(input);

				return info;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}

	private static String streamToString(InputStream input) throws IOException {

		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(isr);

		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		isr.close();
		return sb.toString();
	}
}
