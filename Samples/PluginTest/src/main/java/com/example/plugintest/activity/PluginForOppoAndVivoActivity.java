package com.example.plugintest.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugintest.R;

public class PluginForOppoAndVivoActivity extends AppCompatActivity implements OnClickListener {

	private ViewGroup mRoot;

	private LayoutInflater mInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("测试插件主题");

		setContentView(R.layout.plugin_layout);

		initViews();

		mInflater = getLayoutInflater();

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
		mRoot = (ViewGroup) findViewById(R.id.content);

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
			Toast.makeText(this, getString(R.string.hello_world1), Toast.LENGTH_LONG).show();
		} else if (v.getId() == R.id.plugin_test_btn2) {

		} else if (v.getId() == R.id.plugin_test_btn4) {
			((Button) v).setText(getResources().getString(com.example.pluginsharelib.R.string.share_string_2));
		}
	}

}
