package com.example.pluginmain;

import android.content.Context;
import android.content.Intent;


/**
 * @author cailiming
 * 
 */
public class FragmentHelper {



	public static void startFragmentWithBuildInActivity(Context context, String targetId) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginSampleFragmentActivity.class);
		pluginActivity.putExtra(PluginSampleFragmentActivity.FRAGMENT_ID_IN_PLUGIN, targetId);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}

}
