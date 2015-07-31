package com.plugin.core.ui.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.plugin.util.LogUtil;

/**
 * Stub模式, 用于运行时被插件中的Service替换,这种方式比代理模式更稳定
 * 
 * @author cailiming
 *
 */
public class PluginStubService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.d("PluginStubService", "should not happen");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		LogUtil.d("PluginStubService", "should not happen");

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
