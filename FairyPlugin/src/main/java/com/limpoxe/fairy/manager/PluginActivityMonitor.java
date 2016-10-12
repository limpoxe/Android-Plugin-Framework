package com.limpoxe.fairy.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.limpoxe.fairy.core.PluginClassLoader;
import com.limpoxe.fairy.core.PluginContextTheme;

import java.util.HashMap;

public class PluginActivityMonitor {

	public static final String ACTION_UN_INSTALL_PLUGIN = "com.limpoxe.fairy.action.ACTION_UN_INSTALL_PLUGIN";

	private HashMap<Activity, BroadcastReceiver> receivers = new HashMap<Activity, BroadcastReceiver>();

	public void onActivityCreate(final Activity activity) {
		if (!activity.isChild()) {
			if (activity.getClass().getClassLoader() instanceof PluginClassLoader) {
				String pluginId = ((PluginContextTheme)activity.getApplication().getBaseContext()).getPluginDescriptor().getPackageName();
				BroadcastReceiver br = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						activity.finish();
					}
				};
				receivers.put(activity, br);

				activity.registerReceiver(br, new IntentFilter(pluginId + ACTION_UN_INSTALL_PLUGIN));
			}
		}
	}

	public void onActivityResume(Activity activity) {
		if (!activity.isChild()) {

		}
	}

	public void onActivityPause(Activity activity) {
		if (!activity.isChild()) {

		}
	}

	public void onActivityDestory(Activity activity) {
		if (!activity.isChild()) {
			if (activity.getClass().getClassLoader() instanceof PluginClassLoader) {
				BroadcastReceiver br = receivers.remove(activity);
				activity.unregisterReceiver(br);
			}
		}
	}
}
