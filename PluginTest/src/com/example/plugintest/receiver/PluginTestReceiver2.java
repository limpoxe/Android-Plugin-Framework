package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.plugintest.R;

/**
 * 插件receiver不能监听系统广播
 * 
 * @author cailiming
 * 
 */
public class PluginTestReceiver2 extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Toast.makeText(context, "PluginTestReceiver2 onReceive " + context.getResources().getText(R.string.hello_world4), Toast.LENGTH_LONG).show();
	}

}
