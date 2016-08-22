package com.example.pluginmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.plugin.core.PluginThemeHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class TestSkinActivity extends Activity {

	private static final String SKIN_PLUGIN_ID = "com.example.plugintest";
	private static final String SKIN_KEY = "shinId";
	private static final String SKIN_NAME_2 = "PluginTheme2";
	private static final String SKIN_NAME_4 = "PluginTheme4";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		applyTheme();

		super.onCreate(savedInstanceState);

		setContentView(R.layout.shin_activity);

		setTitle("宿主换肤");

		initView();

	}

	private void applyTheme() {
		int skin = PreferenceManager.getDefaultSharedPreferences(getApplication()).getInt(SKIN_KEY, 0);
		if (skin != 0) {
			PluginThemeHelper.applyPluginTheme(this, SKIN_PLUGIN_ID, skin);
		}
	}

	private void initView() {

		findViewById(R.id.allThemes).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HashMap<String, Integer> themes = PluginThemeHelper.getAllPluginThemes(SKIN_PLUGIN_ID);
				Iterator<Entry<String, Integer>> itr = themes.entrySet().iterator();
				String text = "";
				while (itr.hasNext()) {
					Entry<String, Integer> entry = itr.next();
					text = text + entry.getKey() + ":" + entry.getValue() + "\n";
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(TestSkinActivity.this);
				TextView tv = new TextView(TestSkinActivity.this);
				builder.setView(tv);
				tv.setText(text);
				builder.setTitle("插件可选主题列表");

				AlertDialog dialog = builder.create();
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
		});

		findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				int themeId = PluginThemeHelper.getPluginThemeIdByName(SKIN_PLUGIN_ID, SKIN_NAME_2);
				PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().putInt(SKIN_KEY, themeId).commit();

				if (Build.VERSION.SDK_INT >= 11) {
					//重启使主题生效
					TestSkinActivity.this.recreate();
				}
			}
		});

		findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				int themeId = PluginThemeHelper.getPluginThemeIdByName(SKIN_PLUGIN_ID, SKIN_NAME_4);
				PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().putInt(SKIN_KEY, themeId).commit();

				if (Build.VERSION.SDK_INT >= 11) {
					//重启使主题生效
					TestSkinActivity.this.recreate();
				}
			}
		});

		findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().remove(SKIN_KEY).commit();
				if (Build.VERSION.SDK_INT >= 11) {
					//重启使主题生效
					TestSkinActivity.this.recreate();
				}
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
