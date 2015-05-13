package com.example.plugintest;

import com.plugin.core.PluginLoader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * 完整生命周期模式 不使用反射、也不使用代理，真真正证实现activity无需在Manifest中注册！
 * 
 * @author cailiming
 *
 */
public class PluginNotInManifestActivity extends Activity implements OnClickListener {

	private ViewGroup mRoot;
	private LayoutInflater mInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("测试插件中拥有真正生命周期的Activity");
		mInflater = getLayoutInflater();
		View scrollview = mInflater.inflate(R.layout.plugin_layout, null);

		mRoot = (ViewGroup) scrollview.findViewById(R.id.content);

		initViews();

		setContentView(scrollview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0, 0, "test plugin menu");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast.makeText(this, "test plugin menu", Toast.LENGTH_LONG).show();
		Log.e("xx", "" + item.getTitle());
		return super.onOptionsItemSelected(item);
	}
	
	public void initViews() {

		Button btn1 = (Button) mRoot.findViewById(R.id.plugin_test_btn1);
		btn1.setOnClickListener(this);

		Button btn2 = (Button) mRoot.findViewById(R.id.plugin_test_btn2);
		btn2.setOnClickListener(this);

		Button btn3 = (Button) mRoot.findViewById(R.id.plugin_test_btn3);
		btn3.setOnClickListener(this);

		Button btn4 = (Button) mRoot.findViewById(R.id.plugin_test_btn4);
		btn4.setOnClickListener(this);
 
	}

	@Override
	public void onClick(View v) {
		Log.v("v.click 111", "" + v.getId());
		if (v.getId() == R.id.plugin_test_btn1) {
			View view = mInflater.inflate(R.layout.plugin_layout, null, false);
			mRoot.addView(view);
			((Button) v).setText(R.string.hello_world14);
		} else if (v.getId() == R.id.plugin_test_btn2) {
			View view = mInflater.inflate(com.example.pluginsharelib.R.layout.share_main, null, false);
			mRoot.addView(view);
			((Button) v).setText(R.string.hello_world15);
		} else if (v.getId() == R.id.plugin_test_btn3) {
			View view = LayoutInflater.from(this).inflate(com.example.pluginsharelib.R.layout.share_main, null, false);
			mRoot.addView(view);
		} else if (v.getId() == R.id.plugin_test_btn4) {
			((Button) v).setText(com.example.pluginsharelib.R.string.share_string_2);
		}
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(PluginLoader.getPluginContext(PluginNotInManifestActivity.class));
	}
}
