package com.example.pluginsharelib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.google.gson.Gson;

/**
 * 仅仅用来测试插件程序中是否可以使用宿主程序中的自定义控件和控件的布局文件
 * @author cailiming
 *
 */
public class ShareLayout extends LinearLayout {
	
	public ShareLayout(Context context) {
		super(context);
	}
	
	public ShareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.DiagonalLayout, 0, 0);

		int xx = a.getInt(R.styleable.DiagonalLayout_diagonal_gravity, 0);
		Log.d("xx", "xx=" + xx);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Gson gso = new Gson();
		LayoutInflater.from(getContext()).inflate(R.layout.share_layout, this);
	}
}
