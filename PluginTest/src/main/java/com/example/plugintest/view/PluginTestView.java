package com.example.plugintest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.plugintest.R;

/**
 * 测试控件级插件
 * @author cailiming
 *
 */
public class PluginTestView extends LinearLayout {

	public PluginTestView(Context context) {
		super(context);
	}

	public PluginTestView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater.from(getContext()).inflate(R.layout.plugin_test_view, this);

		findViewById(R.id.btnPlugin).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(PluginTestView.this.getContext(), "测试控件级插件事件", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
