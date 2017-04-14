package com.example.plugintest.activity;

import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.example.plugintest.R;

@SuppressWarnings("ALL")
public class PluginTestTabActivity extends TabActivity {
	private TabHost tabHost;  //声明TabHost
	private Intent intent;  //声明Intent

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_tab_activity);

		tabHost = getTabHost();
		intent = new Intent(this,PluginForDialogActivity.class);
		tabHost.addTab(
				tabHost.newTabSpec("First")
						.setIndicator("选项卡一")
						.setContent(intent));

		intent = new Intent(this,PluginNotInManifestActivity.class);
		tabHost.addTab(
				tabHost.newTabSpec("Second")
						.setIndicator("选项卡二")
						.setContent(intent));
		tabHost.setCurrentTab(0);
	}

	@Override
	public void onBackPressed() {
		getLocalActivityManager()
				.getCurrentActivity().onBackPressed();
	}
}
