package com.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.plugin.core.PluginDescriptor;

public class ApkReader {
	private static final String LOG_TAG = ApkReader.class.getSimpleName();

	private static final String PLUGIN_DESCRIPTION_FILE = "assets/plugin.json";

	public static PluginDescriptor readPluginDescriptor(String pluginFile) {

		Log.d(LOG_TAG, "readPluginDescription:" + pluginFile);
		String pluginDesciption = ApkReader.readFileFromJar(pluginFile, PLUGIN_DESCRIPTION_FILE);
		if (!TextUtils.isEmpty(pluginDesciption)) {
			try {
				JSONObject json = new JSONObject(pluginDesciption);
				PluginDescriptor pluginDescriptor = new PluginDescriptor();
				pluginDescriptor.setId(json.get("id").toString());
				pluginDescriptor.setVersion(json.get("version").toString());
				pluginDescriptor.setDescription(json.get("description").toString());
				
				HashMap<String, String> pluginFragment = new HashMap<String, String>();
				JSONObject fragmentJson = json.getJSONObject("fragments");
				Iterator<String> fragmentIds = fragmentJson.keys();
				while (fragmentIds.hasNext()) {
					String fragmenId = fragmentIds.next();
					pluginFragment.put(fragmenId, fragmentJson.getString(fragmenId));
				}
				pluginDescriptor.setfragments(pluginFragment);

				HashMap<String, String> pluginActiviy = new HashMap<String, String>();
				JSONObject activityJson = json.getJSONObject("activities");
				Iterator<String> activityIds = activityJson.keys();
				while (activityIds.hasNext()) {
					String activityId = activityIds.next();
					pluginActiviy.put(activityId, activityJson.getString(activityId));
				}
				pluginDescriptor.setActivities(pluginActiviy);

				return pluginDescriptor;
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static boolean copyFile(final InputStream inputStream, String dest) {
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
