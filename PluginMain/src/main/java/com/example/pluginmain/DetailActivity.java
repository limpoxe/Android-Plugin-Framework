package com.example.pluginmain;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pluginsharelib.SharePOJO;
import com.plugin.content.PluginDescriptor;
import com.plugin.core.manager.PluginManagerHelper;

import java.util.HashMap;
import java.util.Iterator;

public class DetailActivity extends AppCompatActivity {

	private ViewGroup mRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_activity);

		setTitle("插件详情");

		mRoot = (ViewGroup) findViewById(R.id.root);

		String pluginId = getIntent().getStringExtra("plugin_id");
		if (pluginId == null) {
			Toast.makeText(this, "缺少plugin_id参数", Toast.LENGTH_LONG).show();
			return;
		}

		PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
		if (pluginDescriptor != null) {
			initViews(pluginDescriptor);
		}
	}

	private void initViews(PluginDescriptor pluginDescriptor) {
		TextView pluginIdView = (TextView) mRoot.findViewById(R.id.plugin_id);
		pluginIdView.setText("插件Id：" + pluginDescriptor.getPackageName());

		TextView pluginVerView = (TextView) mRoot.findViewById(R.id.plugin_version);
		pluginVerView.setText("插件Version：" + pluginDescriptor.getVersion());

		TextView pluginDescipt = (TextView) mRoot.findViewById(R.id.plugin_description);
		pluginDescipt.setText("插件Description：" + pluginDescriptor.getDescription());

		TextView pluginInstalled = (TextView) mRoot.findViewById(R.id.plugin_installedPath);
		pluginInstalled.setText("插件安装路径：" + pluginDescriptor.getInstalledPath());

		TextView pluginStandalone = (TextView) mRoot.findViewById(R.id.isstandalone);
		pluginStandalone.setText("独立插件：" + (pluginDescriptor.isStandalone() ? "是" : "否"));


		LinearLayout pluginView = (LinearLayout) mRoot.findViewById(R.id.plugin_items);

		addButton(pluginView, pluginDescriptor.isStandalone(), pluginDescriptor.getFragments(), "Fragment");

		addButton(pluginView, pluginDescriptor.isStandalone(), pluginDescriptor.getActivitys(), "Activity");

		addButton(pluginView, pluginDescriptor.isStandalone(), pluginDescriptor.getServices(), "Service");

		addButton(pluginView, pluginDescriptor.isStandalone(), pluginDescriptor.getReceivers(), "Receiver");
	}

	private void addButton(LinearLayout pluginView, final boolean isStandalone, HashMap map, final String type) {
		Iterator<String> keys = map.keySet().iterator();
		while (keys.hasNext()) {

			final String className = keys.next();

			TextView tv = new TextView(this);
			// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
			tv.append("插件类型：" + type);
			pluginView.addView(tv);

			tv = new TextView(this);
			tv.append("插件ClassName ： " + className);
			pluginView.addView(tv);

			Button btn = new Button(this);
			btn.setText("点击打开");
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
					if (type.equals("Service")) {

						Intent intent = new Intent();
						intent.setClassName(DetailActivity.this, className);
						intent.putExtra("testParam", "testParam");
						if (!isStandalone) {
							intent.putExtra("paramVO", new SharePOJO("测试VO"));
						}
						startService(intent);
						// stopService(intent);

					} else if (type.equals("Receiver")) {// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的

						Intent intent = new Intent();
						intent.setClassName(DetailActivity.this, className);
						intent.putExtra("testParam", "testParam");
						if (!isStandalone) {
							intent.putExtra("paramVO", new SharePOJO("测试VO"));
						}
						sendBroadcast(intent);

					} else if (type.equals("Activity")) {// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的

						Intent intent = new Intent();
						intent.setClassName(DetailActivity.this, className);
						intent.putExtra("testParam", "testParam");
						if (!isStandalone) {
							intent.putExtra("paramVO", new SharePOJO("测试VO"));
						}
						startActivity(intent);

					} else if (type.equals("Fragment")) {
						// 插件中的Fragment分两类
						// 第一类是在插件提供的Activity中展示，就是一个普通的Fragment
						// 第二类是在宿主提供的Activity中展示，分为普通Fragment和特别处理过的fragment
						Intent pluginActivity = new Intent();
						pluginActivity.setClass(DetailActivity.this, TestFragmentActivity.class);
						pluginActivity.putExtra(TestFragmentActivity.FRAGMENT_ID_IN_PLUGIN, className);
						pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(pluginActivity);
					}
				}
			});
			pluginView.addView(btn);

			if (Build.VERSION.SDK_INT >= 14) {
				Space space = new Space(this);
				space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 25));
				pluginView.addView(space);
			}

		}
	}


}
