package com.example.pluginmain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Instrumentation;
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
import com.plugin.core.PluginInstrumentionWrapper;
import com.plugin.core.PluginLoader;
import com.plugin.util.FileUtil;
import com.plugin.util.RefInvoker;

public class PluginListActivity extends Activity {

	private ViewGroup mList;
	private Button install;
	boolean isInstalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		setTitle("插件列表");

		// 监听插件安装 安装新插件后刷新当前页面
		registerReceiver(pluginChange, new IntentFilter("com.plugin.core.action_plugin_changed"));

		initView();

		listAll();
	}

	private void initView() {

		mList = (ViewGroup) findViewById(R.id.list);
		install = (Button) findViewById(R.id.install);

		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isInstalled) {
					isInstalled = true;

					copyAndInstall("PluginTest-debug.apk");
					copyAndInstall("PluginHelloWorld-debug.apk");

				} else {
					Toast.makeText(PluginListActivity.this, "点1次就可以啦！", Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private void copyAndInstall(String name) {
		try {
			InputStream assestInput = getAssets().open(name);
			String dest = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name;
			if (FileUtil.copyFile(assestInput, dest)) {
				PluginLoader.installPlugin(dest);
			} else {
				assestInput = getAssets().open(name);
				dest = getCacheDir().getAbsolutePath() + "/" + name;
				if (FileUtil.copyFile(assestInput, dest)) {
					PluginLoader.installPlugin(dest);
				} else {
					Toast.makeText(PluginListActivity.this, "解压Apk失败" + dest, Toast.LENGTH_LONG).show();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(PluginListActivity.this, "安装失败", Toast.LENGTH_LONG).show();
		}
	}

	private void listAll() {
		ViewGroup root = mList;
		root.removeAllViews();

		// 列出所有已经安装的插件
		Collection<PluginDescriptor> plugins = PluginLoader.getPlugins();
		Iterator<PluginDescriptor> itr = plugins.iterator();
		while (itr.hasNext()) {
			final PluginDescriptor pluginDescriptor = itr.next();
			Button button = new Button(this);
			button.setPadding(10, 10, 10, 10);
			button.setText("插件id：" + pluginDescriptor.getPackageName() + "，点击查看");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PluginListActivity.this, PluginDetailActivity.class);
					intent.putExtra("plugin_id", pluginDescriptor.getPackageName());
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
			Toast.makeText(PluginListActivity.this,
					"插件"  + intent.getStringExtra("id") + " "+ intent.getStringExtra("type") + "完成",
					Toast.LENGTH_SHORT).show();
			listAll();
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginChange);
	};

}
