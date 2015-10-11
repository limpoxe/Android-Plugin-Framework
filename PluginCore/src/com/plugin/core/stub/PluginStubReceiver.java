package com.plugin.core.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.plugin.util.LogUtil;

/**
 * Stub模式, 用于运行时被插件中的BroadcastReceiver替换,这种方式比代理模式更稳定
 * 
 * @author cailiming
 *
 */
public class PluginStubReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.d("PluginStubReceiver", "should not happen");
	}
	
}
