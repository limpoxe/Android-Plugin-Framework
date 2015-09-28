package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.plugintest.vo.ParamVO;
import com.plugin.util.LogUtil;

/**
 * 插件receiver不能监听系统广播
 * @author cailiming
 *
 */
public class PluginTestReceiver extends BroadcastReceiver {

	/**
	 * 这个context是主程序的Context，因此不能使用这个context去访问当前插件的资源
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("xx", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());

		Toast.makeText(context, "PluginTestReceiver onReceive " + intent.toUri(0),
				Toast.LENGTH_LONG).show();
	}

}
