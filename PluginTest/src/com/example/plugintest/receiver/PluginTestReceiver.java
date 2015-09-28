package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.plugintest.R;
import com.example.plugintest.vo.ParamVO;
import com.plugin.util.LogUtil;

/**
 * 插件receiver不能监听系统广播
 * @author cailiming
 *
 */
public class PluginTestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("xx", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());

		Toast.makeText(context, "PluginTestReceiver onReceive " + context.getResources().getText(R.string.hello_world4),
				Toast.LENGTH_LONG).show();
	}

}
