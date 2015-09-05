package com.example.pluginmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.core.PluginLoader;
import com.plugin.util.FragmentHelper;
import com.plugin.util.LogUtil;

public class PluginDetailActivity extends Activity {

	private ViewGroup mRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_activity);
		mRoot = (ViewGroup) findViewById(R.id.root);

		LogUtil.d(getIntent().toUri(0));

		String pluginId = getIntent().getStringExtra("plugin_id");
		if (pluginId == null) {
			Toast.makeText(this, "缺少plugin_id参数", Toast.LENGTH_LONG).show();
			return;
		}
		PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByPluginId(pluginId);

		initViews(pluginDescriptor);
	}

	private void initViews(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor != null) {
			TextView pluginIdView = (TextView) mRoot.findViewById(R.id.plugin_id);
			pluginIdView.setText("插件Id：" + pluginDescriptor.getPackageName());

			TextView pluginVerView = (TextView) mRoot.findViewById(R.id.plugin_version);
			pluginVerView.setText("插件Version：" + pluginDescriptor.getVersion());

			TextView pluginDescipt = (TextView) mRoot.findViewById(R.id.plugin_description);
			pluginDescipt.setText("插件Description：" + pluginDescriptor.getDescription());

			TextView pluginInstalled = (TextView) mRoot.findViewById(R.id.plugin_installedPath);
			pluginInstalled.setText("插件安装路径：" + pluginDescriptor.getInstalledPath());

			LinearLayout pluginFragmentView = (LinearLayout) mRoot.findViewById(R.id.plugin_fragments);
			Iterator<Entry<String, String>> fragment = pluginDescriptor.getFragments().entrySet().iterator();
			while (fragment.hasNext()) {
				final Entry<String, String> entry = fragment.next();

				TextView tv = new TextView(this);
				tv.setText("插件ClassId：" + entry.getKey());
				pluginFragmentView.addView(tv);

				tv = new TextView(this);
				tv.append("插件ClassName ： " + entry.getValue());
				pluginFragmentView.addView(tv);

				tv = new TextView(this);
				tv.append("插件类型：Fragment");
				pluginFragmentView.addView(tv);

				Button btn = new Button(this);
				btn.setText("点击打开");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 插件中的Fragment分两类
						// 第一类是在插件提供的Activity中展示，就是一个普通的Fragment
						// 第二类是在宿主提供的Activity中展示，分为普通Fragment和特别处理过的fragment
						FragmentHelper.startFragmentWithBuildInActivity(PluginDetailActivity.this, entry.getKey());
					}
				});
				pluginFragmentView.addView(btn);
			}

			LinearLayout pluginView = (LinearLayout) mRoot.findViewById(R.id.plugin_activities);

			addButton(pluginView, pluginDescriptor.getActivitys(), "Activity");

			addButton(pluginView, pluginDescriptor.getServices(), "Service");

			addButton(pluginView, pluginDescriptor.getReceivers(), "Receiver");
		}
	}

	private void addButton(LinearLayout pluginView, HashMap<String, ArrayList<PluginIntentFilter>> map, final String type) {
		Iterator<String> components = map.keySet().iterator();
		while (components.hasNext()) {

			final String entry = components.next();

			TextView tv = new TextView(this);
			// tv.setText("插件ClassId：" + entry.getKey());
			// pluginActivitysView.addView(tv);

			tv = new TextView(this);
			tv.append("插件ClassName ： " + entry);
			pluginView.addView(tv);

			tv = new TextView(this);
			// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
			tv.append("插件类型：" + type);
			pluginView.addView(tv);

			Button btn = new Button(this);
			btn.setText("点击打开");
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
					if (type.equals("Service")) {

						Intent intent = new Intent();
						intent.setClassName(PluginDetailActivity.this, entry);
						intent.putExtra("testParam", "testParam");
						startService(intent);
						// stopService(intent);

					} else if (type.equals("Receiver")) {// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的

						Intent intent = new Intent();
						intent.setClassName(PluginDetailActivity.this, entry);
						intent.putExtra("testParam", "testParam");
						sendBroadcast(intent);
					} else if (type.equals("Activity")) {

						// 测试通过ClassName匹配
						Intent intent = new Intent();
						intent.setClassName(PluginDetailActivity.this, entry);
						intent.putExtra("testParam", "testParam");
						startActivity(intent);

						// 测试通过action进行匹配的方式
						if (entry.equals("com.example.plugintest.activity.PluginNotInManifestActivity")) {
							intent = new Intent("test.xyz1");
							intent.putExtra("testParam", "testParam");
							startActivity(intent);
						}

						// 测试通过url进行匹配的方式
						if (entry.equals("com.example.plugintest.activity.PluginNotInManifestActivity")) {
							intent = new Intent(Intent.ACTION_VIEW);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.addCategory(Intent.CATEGORY_BROWSABLE);
							intent.setData(Uri.parse("testscheme://testhost"));
							intent.putExtra("testParam", "testParam");
							startActivity(intent);
						}

					}
				}
			});
			pluginView.addView(btn);
		}
	}
}
