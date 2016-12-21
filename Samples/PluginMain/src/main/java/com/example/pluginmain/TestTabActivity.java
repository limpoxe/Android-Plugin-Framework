package com.example.pluginmain;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.limpoxe.fairy.core.PluginIntentResolver;
import com.limpoxe.fairy.core.PluginLoader;

@SuppressWarnings("ALL")
public class TestTabActivity extends TabActivity {
	private TabHost tabHost;  //声明TabHost
	private Intent intent;  //声明Intent

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_activity);

		long loadingTime = PluginLoader.getMinLoadingTime();
		PluginLoader.setMinLoadingTime(0);//使用tabactivity内嵌插件activity时临时关闭loading界面

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

		PluginLoader.setMinLoadingTime(loadingTime);
	}

	@Override
	public void onBackPressed() {
		getLocalActivityManager()
				.getCurrentActivity().onBackPressed();
	}
}
