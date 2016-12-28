package com.example.plugintest.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.plugintest.R;

/**
 * 仅仅用来测试插件程序找中是否可以使用自定义控件
 * @author cailiming
 *
 */
public class PluginTestLayout extends LinearLayout {
	
	public PluginTestLayout(Context context) {
		super(context);
	}
	
	public PluginTestLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.DiagonalLayout1, 0, 0);

		int xx = a.getInt(R.styleable.DiagonalLayout1_diagonal_gravity1, 0);
		Log.d("xx1", "xx1=" + xx);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater.from(getContext()).inflate(com.example.pluginsharelib.R.layout.share_layout, this);
	}
}
