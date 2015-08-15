package com.example.plugintest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugintest.R;
import com.plugin.core.PluginCompat;
import com.plugin.core.PluginLoader;

/**
 * 这个fragment会被嵌在 宿主程序 提供的activity中展示
 */
public class PluginSpecFragment extends Fragment implements OnClickListener {

	private ViewGroup mRoot;
	private Context pluginContext;
	private LayoutInflater pluginInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().setTitle("测试插件自由模式的Fragment");

		// 默认是宿主程序Application主题
		pluginContext = PluginLoader.getNewPluginContext(PluginSpecFragment.class);
		// 设置主题为插件程序主题
		PluginCompat.setTheme(pluginContext, R.style.PluginTheme, PluginSpecFragment.class);
		// 设置主题为宿主程序主题
		// PluginCompat.setTheme(pluginContext,
		// com.example.pluginsharelib.R.style.ShareTheme,
		// PluginSpecFragment.class);

		pluginInflater = (LayoutInflater) pluginContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View scrollview = pluginInflater.inflate(R.layout.plugin_layout, null);

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
		Log.v("v.click MainFragment", "" + v.getId());
		if (v.getId() == R.id.plugin_test_btn1) {
			View view = pluginInflater.inflate(R.layout.plugin_layout, null, false);
			mRoot.addView(view);
			Toast.makeText(this.getActivity(), pluginContext.getString(R.string.hello_world1), Toast.LENGTH_LONG)
					.show();
		} else if (v.getId() == R.id.plugin_test_btn2) {
			View view = pluginInflater.inflate(com.example.pluginsharelib.R.layout.share_main, null, false);
			mRoot.addView(view);
			Toast.makeText(this.getActivity(), getString(com.example.pluginsharelib.R.string.share_string_1),
					Toast.LENGTH_LONG).show();
		} else if (v.getId() == R.id.plugin_test_btn3) {
			View view = LayoutInflater.from(getActivity()).inflate(com.example.pluginsharelib.R.layout.share_main,
					null, false);
			mRoot.addView(view);
		} else if (v.getId() == R.id.plugin_test_btn4) {
			((Button) v).setText(com.example.pluginsharelib.R.string.share_string_2);
		}
	}
}
