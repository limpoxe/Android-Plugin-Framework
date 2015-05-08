package com.example.pluginmain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.plugin.core.PluginDescriptor;
import com.plugin.core.PluginLoader;

public class PluginListActivity extends Activity {
	
	private ViewGroup mRoot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		// 这行代码应当在Application的onCreate中执行。
		PluginLoader.initLoader(getApplication());
		
		//安装一个插件
		//PluginLoader.installPlugin("/sdcard/test/test.apk");
		
		registerReceiver(pluginChange, new IntentFilter(PluginLoader.ACTION_PLUGIN_CHANGED));
		
		mRoot = (ViewGroup) findViewById(R.id.root);
		
		listAll(mRoot);
	}
	
	private void listAll(ViewGroup root) {
		root.removeAllViews();
		TextView lable = new TextView(this);
		lable.setText("已安装的插件列表：");
		lable.setTextSize(16);
		root.addView(lable);
		
		//列出所有已经安装的插件
		HashMap<String, PluginDescriptor> plugins = PluginLoader.listAll();
		Iterator<Entry<String, PluginDescriptor>> itr = plugins.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, PluginDescriptor> entry  = itr.next();
			Button btton = new Button(this);
			btton.setText("插件id：" + entry.getKey() + 
					"\n插件介绍：" + entry.getValue().getDescription() +
					"\n安装路径：" + entry.getValue().getInstalledPath() +
					"\n点击查看详情>>");
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 10;
			layoutParam.bottomMargin = 10;
			root.addView(btton, layoutParam);
			
			
			btton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent  = new Intent(PluginListActivity.this, PluginDetailActivity.class);
					startActivity(intent);
				}
			});
		}
	}
	
	private BroadcastReceiver pluginChange = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.d("PluginListActivity", intent.toUri(0));
			listAll(mRoot);
		};
	};
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginChange);
	};

}
