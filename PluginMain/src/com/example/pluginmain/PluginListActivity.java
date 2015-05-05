package com.example.pluginmain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.plugin.core.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.ui.PluginDispatcher;

public class PluginListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		LinearLayout root = (LinearLayout) findViewById(R.id.root);
		TextView text = new TextView(this);
		text.setText("注意：插件Fragment如果运行在和它的开发方式不匹配的运行容器中，会出错，\n" +
				"" +
				"如下列表中会对这种异常进行演示");
		text.setTextSize(16);
		root.addView(text);
		
		// 这行代码应当在Application的onCreate中执行。
		PluginLoader.initLoader(getApplication());

		//安装一个插件
		//PluginLoader.installPlugin("/sdcard/test/test.apk");
		
		//列出所有已经安装的插件
		HashMap<String, PluginDescriptor> plugins = PluginLoader.listAll();

		Iterator<Entry<String, PluginDescriptor>> it = plugins.entrySet().iterator();
		while (it.hasNext()) {
			
			//列出fragment
			final Entry<String, PluginDescriptor> item = it.next();
			Iterator<Entry<String, String>> itemItr = item.getValue().getFragments().entrySet().iterator();
			while (itemItr.hasNext()) {
				final Entry<String, String> itemItem = itemItr.next();
				Button btn = new Button(this);
				btn.setText(itemItem.getKey() + " " + itemItem.getValue() + "运行在Normal中");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PluginDispatcher.startFragment(PluginListActivity.this, itemItem.getKey());
					}
				});
				LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);
				llp.setMargins(4, 14, 4, 14);
				root.addView(btn, llp);

				btn = new Button(this);
				btn.setText(itemItem.getKey() + " " + itemItem.getValue() + "运行在Spec中");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PluginDispatcher.startNormalFragment(PluginListActivity.this, itemItem.getKey());
					}
				});
				llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);
				llp.setMargins(4, 14, 4, 14);
				root.addView(btn, llp);
			}

			//列出activity
			itemItr = item.getValue().getActivities().entrySet().iterator();
			while (itemItr.hasNext()) {
				final Entry<String, String> itemItem = itemItr.next();
				Button btn = new Button(this);
				btn.setText(itemItem.getKey() + " " + itemItem.getValue() + "运行在Proxy中");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PluginDispatcher.startActivity(PluginListActivity.this, itemItem.getKey());
					}
				});
				LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);
				llp.setMargins(4, 14, 4, 14);
				root.addView(btn, llp);
 
			}

		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0, 0, "test menu");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast.makeText(this, "text", Toast.LENGTH_LONG).show();
		Log.e("xx", "" + item.getTitle());
		return super.onOptionsItemSelected(item);
	}

}
