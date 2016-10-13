package com.example.pluginmain;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.limpoxe.fairy.core.PluginIntentResolver;

@SuppressWarnings("ALL")
public class TestTabActivity extends TabActivity {
	private TabHost tabHost;  //声明TabHost
	private Intent intent;  //声明Intent

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_activity);

		tabHost = getTabHost();
		intent = new Intent("test.dialogXX");
		PluginIntentResolver.resolveActivity(intent);//修正intent
		tabHost.addTab(
				tabHost.newTabSpec("First")
						.setIndicator("来自插件一")
						.setContent(intent));

		intent = new Intent("test.xyz1");
		PluginIntentResolver.resolveActivity(intent);//修正intent
		tabHost.addTab(
				tabHost.newTabSpec("Second")
						.setIndicator("来自插件二")
						.setContent(intent));
		tabHost.setCurrentTab(0);
	}

	@Override
	public void onBackPressed() {
		getLocalActivityManager()
				.getCurrentActivity().onBackPressed();
	}
}
