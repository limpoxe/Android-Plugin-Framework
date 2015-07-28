package com.plugin.core.ui;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.IBinder;

/**
 * Stub模式, 用于运行时被插件中的BroadcastReceiver替换,这种方式比代理模式更稳定
 * 
 * @author cailiming
 *
 */
public class PluginStubReceiver extends BroadcastReceiver {

	public static final String ACTION = "com.plugin.core.ui.ACTION_STUB_RECEIVER";
	
	@Override
	public void onReceive(Context context, Intent intent) {
	}
	
}
