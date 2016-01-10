package com.example.plugintest.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.pluginsharelib.SharePOJO;
import com.example.plugintest.service.PluginTestService;
import com.example.plugintest.vo.ParamVO;

public class PluginTestOpenPluginActivity extends Activity implements OnClickListener {

	NestReceiver nestReceiver;
	NestReceiver2 nestReceiver2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button btn = new Button(this);
		btn.setText("点击测试打开插件Activity、service、receiver");
		setContentView(btn);
		btn.setOnClickListener(this);

		Log.d("paramVO", ((SharePOJO) getIntent().getSerializableExtra("paramVO")).name);

		IntentFilter testFiler = new IntentFilter();
		testFiler.addAction("xx.nest");
		nestReceiver = new NestReceiver();
		registerReceiver(nestReceiver, testFiler);

		IntentFilter testFiler2 = new IntentFilter();
		testFiler2.addAction("xx.nest");
		nestReceiver2 = new NestReceiver2();
		registerReceiver(nestReceiver2, testFiler2);
	}

	class NestReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("NestReceiver", intent.getStringExtra("str1") + ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());
		}
	}

	class NestReceiver2 extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("NestReceiver2", ((ParamVO) intent.getSerializableExtra("paramvo")) + ", action:" + intent.getAction());
		}
	}

	@Override
	public void onClick(View v) {
		Intent testIntent = new Intent(this, PluginTestActivity.class);
		testIntent.putExtra("str1", "打开PluginTestActivity——————");
		ParamVO pvo = new ParamVO();
		pvo.name = "打开PluginTestActivity";
		testIntent.putExtra("paramvo", pvo);
		startActivity(testIntent);


		testIntent = new Intent("test.abc");
		testIntent.putExtra("str1", "打开test.abc——————");
		pvo = new ParamVO();
		pvo.name = "打开test.abc";
		testIntent.putExtra("paramvo", pvo);
		startActivity(testIntent);

		//启动服务
		Intent service = new Intent();
		service.setClassName(this, PluginTestService.class.getName());
		service.putExtra("str1", "打开PluginTestService——————");
		pvo = new ParamVO();
		pvo.name = "打开PluginTestService";
		service.putExtra("paramvo", pvo);
		startService(service);

		//停止服务
		service = new Intent();
		service.setClassName(this, PluginTestService.class.getName());
		service.putExtra("str1", "停止PluginTestService——————");
		pvo = new ParamVO();
		pvo.name = "停止PluginTestService";
		service.putExtra("paramvo", pvo);
		stopService(service);


		Intent intent = new Intent("test.rst2");
		intent.putExtra("str1", "打开 test.rst2——————");
		pvo = new ParamVO();
		pvo.name = "打开 test.rst2";
		intent.putExtra("paramvo", pvo);
		sendBroadcast(intent);

		//测试动态注册的插件广播
		intent = new Intent("xx.nest");
		intent.putExtra("str1", "打开动态注册的NestReceiver——————");
		pvo = new ParamVO();
		pvo.name = "打开动态注册的NestReceiver";
		intent.putExtra("paramvo", pvo);
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(nestReceiver);
	}
}
