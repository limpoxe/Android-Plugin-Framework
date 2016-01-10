package com.example.plugintest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugintestbase.ILoginService;
import com.example.plugintestbase.LoginVO;

public class PluginSingleTaskActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button btn = new Button(this);
		btn.setText("反复点击，测试插件SingleTask");
		setContentView(btn);
		btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		ILoginService login = (ILoginService) getSystemService("login_service");
		if (login != null) {
			LoginVO vo = login.login("admin", "123456");
			Toast.makeText(this, vo.getUsername() + ":" + vo.getPassword(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "ILoginService == null", Toast.LENGTH_SHORT).show();
		}

		startActivity(new Intent(this, PluginSingleTaskActivity.class));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Toast.makeText(this, "onNewIntent Called!!", Toast.LENGTH_SHORT).show();
	}
}
