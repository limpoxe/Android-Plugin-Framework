package com.example.plugintest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;
 
public class PluginFragmentTestActivity extends FragmentActivity {

	private static final String LOG_TAG = PluginFragmentTestActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("测试插件中的FragmentActivity");
		FrameLayout root = new FrameLayout(this);
		setContentView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		root.setId(android.R.id.primary);

		Fragment fragment = new TestFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.primary, fragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0, 0, "test plugin menu");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast.makeText(this, "test plugin menu", Toast.LENGTH_LONG).show();
		Log.e(LOG_TAG, "" + item.getTitle());
		return super.onOptionsItemSelected(item);
	}
	
	public static class TestFragment extends Fragment implements OnClickListener {

		private ViewGroup mRoot;
		private LayoutInflater mInflater;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mInflater = getActivity().getLayoutInflater();
			View scrollview = mInflater.inflate(R.layout.plugin_layout, null);

			mRoot = (ViewGroup) scrollview.findViewById(R.id.content);

			initViews();

			return scrollview;
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
				View view = mInflater.inflate(R.layout.plugin_layout, null,
						false);
				mRoot.addView(view);
				((Button) v).setText(R.string.hello_world14);
			} else if (v.getId() == R.id.plugin_test_btn2) {
				
				View view = mInflater
						.inflate(com.example.pluginsharelib.R.layout.share_main,
								null, false);
				mRoot.addView(view);
				((Button) v).setText(R.string.hello_world15);
			} else if (v.getId() == R.id.plugin_test_btn3) {
				View view = LayoutInflater.from(getActivity())
						.inflate(com.example.pluginsharelib.R.layout.share_main,
								null, false);
				mRoot.addView(view);
			} else if (v.getId() == R.id.plugin_test_btn4) {
				((Button) v).setText(com.example.pluginsharelib.R.string.share_string_2);
			}
		}
	}


}
