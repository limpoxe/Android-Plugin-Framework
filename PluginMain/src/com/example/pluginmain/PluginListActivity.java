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
import android.widget.Toast;

import com.plugin.core.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.ui.PluginDispatcher;

public class PluginListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// 这行代码应当在Application的onCreate中执行。
		PluginLoader.initLoader(getApplication());

		HashMap<String, PluginDescriptor> plugins = PluginLoader.listAll();

		LinearLayout root = (LinearLayout) findViewById(R.id.root);
		Iterator<Entry<String, PluginDescriptor>> it = plugins.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<String, PluginDescriptor> item = it.next();
			Iterator<Entry<String, String>> itemItr = item.getValue().getFragments().entrySet().iterator();
			while (itemItr.hasNext()) {
				final Entry<String, String> itemItem = itemItr.next();
				Button btn = new Button(this);
				btn.setText(itemItem.getKey() + " " + itemItem.getValue());
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PluginDispatcher.startFragment(PluginListActivity.this, itemItem.getKey());
					}
				});
				root.addView(btn);

				btn = new Button(this);
				btn.setText(itemItem.getKey() + " " + itemItem.getValue());
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PluginDispatcher.startNormalFragment(PluginListActivity.this, itemItem.getKey());
					}
				});
				root.addView(btn);
			}

			Button btn = new Button(this);
			btn.setText("test3" + " : com.example.plugintest.PluginTextActivity ");
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					PluginDispatcher.startActivity(PluginListActivity.this, "test3");
				}
			});
			root.addView(btn);

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
