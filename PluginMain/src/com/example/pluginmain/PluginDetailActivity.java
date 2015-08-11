package com.example.pluginmain;

import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginDispatcher;
import com.plugin.core.PluginLoader;

public class PluginDetailActivity extends Activity {

	private ViewGroup mRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_activity);
		mRoot = (ViewGroup) findViewById(R.id.root);

		String pluginId = getIntent().getStringExtra("plugin_id");
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
				btn.append("点击打开");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// if ("vivo".equalsIgnoreCase(Build.BRAND) ||
						// "oppo".equalsIgnoreCase(Build.BRAND)) {
						// 要想步步高手机也支持fragment模式,插件需要使用独立模式，即不通过R直接或者间接使用注宿主程序的资源
						// 如果步步高手机也需要使用非独立插件，则要舍弃宿主主题
						// Toast.makeText(PluginDetailActivity.this,
						// "步步高手机暂时只支持Activity模式！", Toast.LENGTH_LONG).show();
						// return;
						// }
						// 两种fragment模式,
						// 一种是显示在内建的Fragment容器中，一种是可嵌入在任意支持fragment的页面
						if (entry.getValue().equals("com.example.plugintest.fragment.PluginNormalFragment")) {
							PluginDispatcher.startFragmentWithBuildInActivity(PluginDetailActivity.this, entry.getKey());
						} else {
							PluginDispatcher.startFragmentWithBuildInActivity(PluginDetailActivity.this, entry.getKey());
							PluginDispatcher.startFragmentWithSimpleActivity(PluginDetailActivity.this, entry.getKey());
						}

					}
				});
				pluginFragmentView.addView(btn);
			}

			LinearLayout pluginActivitysView = (LinearLayout) mRoot.findViewById(R.id.plugin_activities);
			Iterator<String> components = pluginDescriptor.getComponents().keySet().iterator();
			while (components.hasNext()) {

				final String entry = components.next();

				TextView tv = new TextView(this);
				// tv.setText("插件ClassId：" + entry.getKey());
				// pluginActivitysView.addView(tv);

				tv = new TextView(this);
				tv.append("插件ClassName ： " + entry);
				pluginActivitysView.addView(tv);

				tv = new TextView(this);
				// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
				tv.append("插件类型："
						+ (entry.contains("Service") ? "service" : (entry.contains("Receiver") ? "Receiver"
								: "activity")));
				pluginActivitysView.addView(tv);

				Button btn = new Button(this);
				btn.append("点击打开");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// oppo 和 vivo 手机单独处理
						// if ("vivo".equalsIgnoreCase(Build.BRAND) ||
						// "oppo".equalsIgnoreCase(Build.BRAND)) {
						// return;
						// }

						// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的
						if (entry.contains("Service")) {

							Intent intent = new Intent();
							intent.setClassName(PluginDetailActivity.this, entry);
							intent.putExtra("testParam", "testParam");
							PluginDispatcher.startService(PluginDetailActivity.this, intent);

						} else if (entry.contains("Receiver")) {// 这个判断仅仅是为了方便debug，在实际开发中，类型一定是已知的

							Intent intent = new Intent();
							intent.setClassName(PluginDetailActivity.this, entry);
							intent.putExtra("testParam", "testParam");
							PluginDispatcher.sendBroadcast(PluginDetailActivity.this, intent);

						} else {
							// PluginDispatcher.startProxyActivity(PluginDetailActivity.this,
							// entry.getKey());

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
				pluginActivitysView.addView(btn);

			}
		}
	}
}
