package com.example.pluginmain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.FileUtil;

public class PluginListActivity extends Activity {

	private ViewGroup mList;
	private Button install;
	boolean isInstalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// 这行代码应当在Application的onCreate中执行。
		PluginLoader.initLoader(getApplication());

		// 监听插件安装 安装新插件后刷新当前页面
		registerReceiver(pluginChange, new IntentFilter(PluginLoader.ACTION_PLUGIN_CHANGED));

		initView();
	}

	private void initView() {
		install = (Button) findViewById(R.id.install);
		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isInstalled) {
					isInstalled = true;

					copyAndInstall("PluginTest-debug.apk");
					copyAndInstall("HelloWork.apk");
					// copyAndInstall("Game1-debug.apk");

				}
			}
		});

		mList = (ViewGroup) findViewById(R.id.list);
		listAll(mList);
	}

	private void copyAndInstall(String name) {
		try {
			InputStream assestInput = getAssets().open(name);
			String sdcardDest = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name;
			if (FileUtil.copyFile(assestInput, sdcardDest)) {
				PluginLoader.installPlugin(sdcardDest);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(PluginListActivity.this, "安装失败", Toast.LENGTH_LONG).show();
		}
	}

	private void listAll(ViewGroup root) {
		root.removeAllViews();

		// 列出所有已经安装的插件
		Hashtable<String, PluginDescriptor> plugins = PluginLoader.listAll();
		Iterator<Entry<String, PluginDescriptor>> itr = plugins.entrySet().iterator();
		while (itr.hasNext()) {
			final Entry<String, PluginDescriptor> entry = itr.next();
			Button button = new Button(this);
			button.setPadding(10, 10, 10, 10);
			button.setText("插件id：" + entry.getKey() + "，点击查看");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PluginListActivity.this, PluginDetailActivity.class);
					intent.putExtra("plugin_id", entry.getKey());
					startActivity(intent);
				}
			});

			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 10;
			layoutParam.bottomMargin = 10;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
		}
	}

	private final BroadcastReceiver pluginChange = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(PluginListActivity.this, "插件" + intent.getStringExtra(PluginLoader.EXTRA_TYPE) + "成功",
					Toast.LENGTH_LONG).show();
			listAll(mList);
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginChange);
	};

}
