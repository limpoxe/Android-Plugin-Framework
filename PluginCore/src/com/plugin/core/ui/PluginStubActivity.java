package com.plugin.core.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Stub模式, 用于运行时被插件中的activity替换,这种方式比代理模式更稳定
 * 
 * @author cailiming
 *
 */
public class PluginStubActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("PluginStubActivity", "should not happen");
	}
}
