package com.example.pluginmain;

import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plugin.core.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.ui.PluginDispatcher;

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
			TextView pluginIdView = (TextView)mRoot.findViewById(R.id.plugin_id);
			pluginIdView.setText("插件Id：" + pluginDescriptor.getId());
			
			TextView pluginVerView = (TextView)mRoot.findViewById(R.id.plugin_version);
			pluginVerView.setText("插件Version：" + pluginDescriptor.getVersion());
			
			TextView pluginDescipt = (TextView)mRoot.findViewById(R.id.plugin_description);
			pluginDescipt.setText("插件Description：" + pluginDescriptor.getDescription());
			
			TextView pluginInstalled = (TextView)mRoot.findViewById(R.id.plugin_installedPath);
			pluginInstalled.setText("插件安装路径：" + pluginDescriptor.getInstalledPath());
			
			LinearLayout pluginFragmentView = (LinearLayout)mRoot.findViewById(R.id.plugin_fragments);
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
						//两种fragment模式
						if (!entry.getKey().equals("test1")) {
							PluginDispatcher.startFragmentWithSimpleActivity(PluginDetailActivity.this, entry.getKey());
						}
						PluginDispatcher.startFragmentWithBuildInActivity(PluginDetailActivity.this, entry.getKey());
					}
				});
				pluginFragmentView.addView(btn);
			}
			
			LinearLayout pluginActivitysView = (LinearLayout)mRoot.findViewById(R.id.plugin_activities);
			Iterator<Entry<String, String>> activity = pluginDescriptor.getActivities().entrySet().iterator();
			while (activity.hasNext()) {

				final Entry<String, String> entry = activity.next();
				
				TextView tv = new TextView(this);
				tv.setText("插件ClassId：" + entry.getKey());
				pluginActivitysView.addView(tv);
				
				
				tv = new TextView(this);
				tv.append("插件ClassName ： " + entry.getValue());
				pluginActivitysView.addView(tv);
				
				
				tv = new TextView(this);
				tv.append("插件类型：Activity");
				pluginActivitysView.addView(tv);
				
				
				Button btn = new Button(this);
				btn.append("点击打开");
				btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//Activity两种模式
						PluginDispatcher.startProxyActivity(PluginDetailActivity.this, entry.getKey());
						
						//test5是自由模式开发的
						if (entry.getKey().equals("test5")) {
							PluginDispatcher.startRealActivity(PluginDetailActivity.this, entry.getKey());
						}
					}
				});
				pluginActivitysView.addView(btn);
			
			}
			
		}
	}
}
