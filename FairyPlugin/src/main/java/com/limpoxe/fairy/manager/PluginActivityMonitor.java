package com.limpoxe.fairy.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.limpoxe.fairy.core.PluginContextTheme;
import com.limpoxe.fairy.core.RealPluginClassLoader;
import com.limpoxe.fairy.util.LogUtil;

import java.util.HashMap;

public class PluginActivityMonitor {

	public static final String ACTION_STOP_PLUGIN = ".fairy.action.ACTION_STOP_PLUGIN";

	private HashMap<Activity, BroadcastReceiver> receivers = new HashMap<Activity, BroadcastReceiver>();

	public void onActivityCreate(final Activity activity) {
		if (!activity.isChild()) {
			if (activity.getClass().getClassLoader() instanceof RealPluginClassLoader) {
				String pluginPackageName = ((PluginContextTheme)activity.getApplication().getBaseContext()).getPluginDescriptor().getPackageName();
				BroadcastReceiver br = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						LogUtil.w("onReceive", intent.getAction(), "activity.finish()");
						activity.finish();
					}
				};
				receivers.put(activity, br);

				LogUtil.v("registerReceiver", pluginPackageName + ACTION_STOP_PLUGIN);
				activity.registerReceiver(br, new IntentFilter(pluginPackageName + ACTION_STOP_PLUGIN));
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
			if (activity.getClass().getClassLoader() instanceof RealPluginClassLoader) {
				BroadcastReceiver br = receivers.remove(activity);
				LogUtil.v("unregisterReceiver", br.getClass().getName());
				activity.unregisterReceiver(br);
			}
		}
	}
}
