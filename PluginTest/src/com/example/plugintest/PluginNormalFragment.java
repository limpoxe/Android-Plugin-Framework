package com.example.plugintest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 插件中引用主程序资源文件需要显式的指定引用的R 因为主程序的资源id每次编译时都会变化 所以使用主程序资源id的时候必须使用引用
 * 而不是id的const常量， 因此在插件工程code-gen时没有合并依赖库的R，（合并后会获得依赖库R的id常量）
 * 而是将依赖库的R文件作为编译时的classpath引用 反编译插件可以看到，插件资源id都替换成了常量，二非插件id还是保留R.id的引用形式
 * 这正是我们想要的结果
 * 
 * 
 * 不携带任何插件相关的代理 完全就是一个普通的fragment
 * @author cailiming
 * 
 */
public class PluginNormalFragment extends Fragment implements OnClickListener {

	private ViewGroup mRoot;
	private LayoutInflater mInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		getActivity().setTitle("测试插件中非自由模式的Fragment");

		mInflater = inflater;
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

		Button btn5 = (Button) mRoot.findViewById(R.id.plugin_test_btn5);
		btn5.setOnClickListener(this);

		Button btn6 = (Button) mRoot.findViewById(R.id.plugin_test_btn6);
		btn6.setOnClickListener(this);
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
			//((Button) v).setText(R.string.hello_world17);/////////////////////////////
		} else if (v.getId() == R.id.plugin_test_btn4) {
			((Button) v).setText(com.example.pluginsharelib.R.string.share_string_2);///////////////////////////////
		} else if (v.getId() == R.id.plugin_test_btn5) {
			//((Button) v).setText(R.string.hello_world18);
			//getActivity().getResources().getString(R.string.hello_world18);
		} else if (v.getId() == R.id.plugin_test_btn6) {
			//pluginContext.getResources().getString(R.string.hello_world19);
			//((Button) v).setText(pluginContext.getString(R.string.hello_world19));
		}
	}
}
