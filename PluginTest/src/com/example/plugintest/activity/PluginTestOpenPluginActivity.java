package com.example.plugintest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.plugintest.receiver.PluginTestReceiver;
import com.example.plugintest.service.PluginTestService;

public class PluginTestOpenPluginActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button btn = new Button(this);
		btn.setText("点击测试打开插件Activity、service、receiver");
		setContentView(btn);
		btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		startActivity(new Intent(this, PluginTestActivity.class));

		Intent service = new Intent();
		service.setClassName(this, PluginTestService.class.getName());
		service.putExtra("testParam", "testParam");
		startService(service);

		Intent intent = new Intent();
		intent.setClassName(this, PluginTestReceiver.class.getName());
		intent.putExtra("testParam", "testParam");
		sendBroadcast(intent);
	}

}
