package com.example.pluginmain;

import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
			
			LinearLayout pluginFragmentView = (LinearLayout)mRoot.findViewById(R.id.plugin_fragments);
			Iterator<Entry<String, String>> fragment = pluginDescriptor.getFragments().entrySet().iterator();
			while (fragment.hasNext()) {
				final Entry<String, String> entry = fragment.next();
				TextView tv = new TextView(this);
				tv.setText("插件ClassId：" + entry.getKey());
				tv.append("\n插件ClassName：" + entry.getValue());
				tv.append("\n插件Class类型：Fragment");
				tv.append("\n点击打开>>");
				tv.append("\n");
				tv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//PluginDispatcher.startFragment(PluginDetailActivity.this, entry.getKey());
						PluginDispatcher.startNormalFragment(PluginDetailActivity.this, entry.getKey());
					}
				});
				pluginFragmentView.addView(tv);
			}
			
			LinearLayout pluginActivitysView = (LinearLayout)mRoot.findViewById(R.id.plugin_activities);
			Iterator<Entry<String, String>> activity = pluginDescriptor.getActivities().entrySet().iterator();
			while (activity.hasNext()) {
				final Entry<String, String> entry = activity.next();
				TextView tv = new TextView(this);
				tv.setText("插件ClassId：" + entry.getKey());
				tv.append("\n插件ClassName：" + entry.getValue());
				tv.append("\n插件Class类型：Activity");
				tv.append("\n点击打开>>");
				tv.append("\n");
				tv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						PluginDispatcher.startActivity(PluginDetailActivity.this, entry.getKey());
						if (entry.getKey().equals("test5")) {
							PluginDispatcher.startRealActivity(PluginDetailActivity.this, entry.getKey());
						}
					}
				});
				pluginActivitysView.addView(tv);
			}
			
		}
	}
}
