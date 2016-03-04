package com.example.pluginmain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.example.pluginsharelib.SharePOJO;
import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.annotation.ComponentContainer;
import com.plugin.core.manager.PluginCallback;
import com.plugin.util.FileUtil;

/**
 * 添加这个注解@ComponentContainer是为了控制宿主的当前Activity是否需要支持控件级插件
 *
 * 控件级插件功能默认是关闭的。控件级插件和主题换肤功能不能共存。关闭控件级插件。页面换肤功能刚能生效
 *
 */
@ComponentContainer
public class MainActivity extends AppCompatActivity {

	private ViewGroup mList;
	private Button install;
	boolean isInstalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		setTitle("插件列表");

		initView();

		listAll();

		// 监听插件安装 安装新插件后刷新当前页面
		registerReceiver(pluginInstallEvent, new IntentFilter(PluginCallback.ACTION_PLUGIN_CHANGED));
	}

	private void initView() {
		mList = (ViewGroup) findViewById(R.id.list);
		install = (Button) findViewById(R.id.install);

		final Handler handler = new Handler();

		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isInstalled) {
					isInstalled = true;

					try {
						String[] files = getAssets().list("");
						for (String apk : files) {
							if (apk.endsWith(".apk")) {
								copyAndInstall(apk);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.this, "点1次就可以啦！", Toast.LENGTH_LONG).show();
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
					Toast.makeText(MainActivity.this, "解压Apk失败" + dest, Toast.LENGTH_LONG).show();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_LONG).show();
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
			button.setPadding(10, 25, 10, 25);
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);

			button.setText("打开插件：" + pluginDescriptor.getPackageName());
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pluginDescriptor.getPackageName());
					if (launchIntent == null) {
						Toast.makeText(MainActivity.this, "插件"  + pluginDescriptor.getPackageName() + "没有配置Launcher", Toast.LENGTH_SHORT).show();
						//没有找到Launcher，打开插件详情
						Intent intent = new Intent(MainActivity.this, DetailActivity.class);
						intent.putExtra("plugin_id", pluginDescriptor.getPackageName());
						startActivity(intent);
					} else {
						//打开插件的Launcher界面
						if (!pluginDescriptor.isStandalone()) {
							//测试向非独立插件传宿主中定义的VO对象
							launchIntent.putExtra("paramVO", new SharePOJO("宿主传过来的测试VO"));
						}
						startActivity(launchIntent);
					}

					//也可以直接构造Intent，指定打开插件中的某个Activity
					//Intent intent = new Intent("test.abc");
					//startActivity(intent);
				}
			});
		}

		if (plugins.size() >0) {
			Button button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("打开皮肤测试");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TestSkinActivity.class);
					startActivity(intent);
				}
			});

			button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("打开控件级插件测试");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TestViewActivity.class);
					startActivity(intent);
				}
			});
		}


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginInstallEvent);
	};

	private final BroadcastReceiver pluginInstallEvent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MainActivity.this,
					"插件"  + intent.getStringExtra("id") + " "+ intent.getStringExtra("type") + "完成",
					Toast.LENGTH_SHORT).show();
			listAll();
		};
	};

}
