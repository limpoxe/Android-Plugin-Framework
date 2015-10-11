package com.plugin.util;

import android.content.Context;
import android.content.Intent;

import com.plugin.core.stub.ui.PluginSampleFragmentActivity;

/**
 * @author cailiming
 * 
 */
public class FragmentHelper {

	public static final String FRAGMENT_ID_IN_PLUGIN = "PluginDispatcher.fragmentId";

	public static void startFragmentWithBuildInActivity(Context context, String targetId) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginSampleFragmentActivity.class);
		pluginActivity.putExtra(FRAGMENT_ID_IN_PLUGIN, targetId);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}

}
