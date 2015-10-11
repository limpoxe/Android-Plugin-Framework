package com.plugin.core;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.plugin.core.stub.ui.PluginStubActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LaunchMode动态绑定，解决singleTask问题。
 */
public class PluginStubBinding {

	public static final String STUB_ACTIVITY_PRE = "com.plugin.core.stub.ui.";

	private static final String ACTION_LAUNCH_MODE = "com.plugin.core.LAUNCH_MODE";

	private static HashMap<String, String> singleTaskMapping = new HashMap<String, String>();
	private static HashMap<String, String> singleTopMapping = new HashMap<String, String>();
	private static HashMap<String, String> singleInstanceMapping = new HashMap<String, String>();

	private static boolean isPoolInited = false;

	public static String getLaunchModeStubActivity(String pluginActivityClassName, int launchMode) {

		initPool();

		String stubActivityName = null;

		Iterator<Map.Entry<String, String>> itr = null;

		if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

			itr = singleTaskMapping.entrySet().iterator();

		} else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

			itr = singleTopMapping.entrySet().iterator();

		} else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

			itr = singleInstanceMapping.entrySet().iterator();

		}

		if (itr != null) {

			String idleStubActivityName = null;

			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				if (entry.getValue() == null) {
					if (idleStubActivityName == null) {
						idleStubActivityName = entry.getKey();
					}
				} else if (pluginActivityClassName.equals(entry.getValue())) {
					return entry.getKey();
				}
			}

			//没有绑定到StubActivity，而且还有空余的stubActivity，进行绑定
			if (idleStubActivityName != null) {
				singleTaskMapping.put(idleStubActivityName, pluginActivityClassName);
				return idleStubActivityName;
			}

		}

		//绑定失败
		return PluginStubActivity.class.getName();
	}

	private static void initPool() {
		if (isPoolInited) {
			return;
		}

		Intent launchModeIntent = new Intent();
		launchModeIntent.setAction(ACTION_LAUNCH_MODE);
		launchModeIntent.setPackage(PluginLoader.getApplicatoin().getPackageName());

		List<ResolveInfo> list = PluginLoader.getApplicatoin().getPackageManager().queryIntentActivities(launchModeIntent, PackageManager.MATCH_DEFAULT_ONLY);

		if (list != null && list.size() >0) {
			for (ResolveInfo resolveInfo:
					list) {
				if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

					singleTaskMapping.put(resolveInfo.activityInfo.name, null);

				} else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

					singleTopMapping.put(resolveInfo.activityInfo.name, null);

				} else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

					singleInstanceMapping.put(resolveInfo.activityInfo.name, null);

				}
			}
		}

		isPoolInited = true;
	}
}
