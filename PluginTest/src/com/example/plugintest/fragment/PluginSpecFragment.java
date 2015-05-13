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
import com.example.plugintest.R.id;
import com.example.plugintest.R.layout;
import com.example.plugintest.R.string;
import com.plugin.core.PluginLoader;

/**
 * 插件中引用主程序资源文件需要显式的指定引用的R 因为主程序的资源id每次编译时都会变化 所以使用主程序资源id的时候必须使用引用
 * 而不是id的const常量， 因此在插件工程code-gen时没有合并依赖库的R，（合并后会获得依赖库R的id常量）
 * 而是将依赖库的R文件作为编译时的classpath引用 反编译插件可以看到，插件资源id都替换成了常量，二非插件id还是保留R.id的引用形式
 * 这正是我们想要的结果
 * 
 * 携带插件相关的代码所有使用context的地方，需要使用PluginLoader.getPluginContext()获取的Context
 * 
 * @author cailiming
 * 
 */
public class PluginSpecFragment extends Fragment implements OnClickListener {

	private ViewGroup mRoot;
	private Context pluginContext;
	private LayoutInflater pluginInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActivity().setTitle("测试插件自由模式的Fragment");
		
		pluginContext = PluginLoader.getPluginContext(PluginSpecFragment.class);
		//设置主题为宿主程序主题
		pluginContext.setTheme(getActivity().getApplicationInfo().theme);
		//pluginContext.setTheme(R.style.AppTheme);

		pluginInflater = (LayoutInflater) pluginContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View scrollview = pluginInflater.inflate(R.layout.plugin_layout,
				null);

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
			View view = pluginInflater.inflate(R.layout.plugin_layout, null,
					false);
			mRoot.addView(view);
			Toast.makeText(this.getActivity(), getString(R.string.hello_world1), Toast.LENGTH_LONG).show();
		} else if (v.getId() == R.id.plugin_test_btn2) {
			View view = pluginInflater
					.inflate(com.example.pluginsharelib.R.layout.share_main,
							null, false);
			mRoot.addView(view);
			Toast.makeText(this.getActivity(), getString(com.example.pluginsharelib.R.string.share_string_1), Toast.LENGTH_LONG).show();
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
