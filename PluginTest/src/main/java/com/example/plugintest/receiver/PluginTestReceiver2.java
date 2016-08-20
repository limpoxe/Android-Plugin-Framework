package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.plugintest.R;
import com.example.plugintest.vo.ParamVO;

/**
 * 静态注册的插件receiver不能监听系统广播
 * 
 * @author cailiming
 * 
 */
public class PluginTestReceiver2 extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("PluginTestReceiver2", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());
		Toast.makeText(context, "PluginTestReceiver2 onReceive " + context.getResources().getText(R.string.hello_world4), Toast.LENGTH_LONG).show();
	}

}
