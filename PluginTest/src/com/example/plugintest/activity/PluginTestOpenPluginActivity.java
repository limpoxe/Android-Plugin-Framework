package com.example.plugintest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.pluginsharelib.SharePOJO;
import com.example.plugintest.receiver.PluginTestReceiver;
import com.example.plugintest.service.PluginTestService;
import com.example.plugintest.vo.ParamVO;

public class PluginTestOpenPluginActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button btn = new Button(this);
		btn.setText("点击测试打开插件Activity、service、receiver");
		setContentView(btn);
		btn.setOnClickListener(this);

		Log.d("paramVO", ((SharePOJO)getIntent().getSerializableExtra("paramVO")).name);
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


		Intent intent = new Intent();
		intent.setClassName(this, PluginTestReceiver.class.getName());
		intent.putExtra("str1", "打开PluginTestReceiver——————");
		pvo = new ParamVO();
		pvo.name = "打开PluginTestReceiver";
		intent.putExtra("paramvo", pvo);
		sendBroadcast(intent);
	}

}
