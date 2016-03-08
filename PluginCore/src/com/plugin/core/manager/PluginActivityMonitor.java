package com.plugin.core.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.plugin.core.PluginClassLoader;
import com.plugin.core.PluginContextTheme;

import java.util.HashMap;

public class PluginActivityMonitor {

	public static final String ACTION_UN_INSTALL_PLUGIN = "com.plugin.core.action.ACTION_UN_INSTALL_PLUGIN";

	private HashMap<Activity, BroadcastReceiver> receivers = new HashMap<Activity, BroadcastReceiver>();

	public void onActivityCreate(final Activity activity) {
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

	public void onActivityDestory(Activity activity) {
		if (activity.getClass().getClassLoader() instanceof PluginClassLoader) {
			BroadcastReceiver br = receivers.remove(activity);
			activity.unregisterReceiver(br);
		}
	}
}
