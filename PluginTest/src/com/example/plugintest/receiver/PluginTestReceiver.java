package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * 插件receiver不能监听系统广播
 * @author cailiming
 *
 */
public class PluginTestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Toast.makeText(context, "PluginTestReceiver onReceive " + intent.toUri(0),
				Toast.LENGTH_LONG).show();
	}

}
