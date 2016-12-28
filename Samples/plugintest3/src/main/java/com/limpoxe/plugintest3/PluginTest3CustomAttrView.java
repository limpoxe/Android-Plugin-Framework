package com.limpoxe.plugintest3;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 测试插件程序找中是否可以使用自定义控件自定义属性
 * @author cailiming
 *
 */
public class PluginTest3CustomAttrView extends LinearLayout {

	private String attrText;
	private int attrColor;
	private float attrSize;
	private int ntsGravity;

	public PluginTest3CustomAttrView(Context context) {
		super(context);
	}

	public PluginTest3CustomAttrView(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.Test3DeclareStyleable, 0, 0);

		attrText = a.getString(R.styleable.Test3DeclareStyleable_test3_text);
		attrColor = a.getColor(R.styleable.Test3DeclareStyleable_test3_text_color, 0);
		attrSize = a.getDimension(R.styleable.Test3DeclareStyleable_test3_text_color_size, 0);
		ntsGravity = a.getInt(R.styleable.Test3DeclareStyleable_test3_nts_gravity, -1);

		Log.d("xx", "attrText=" + attrText);
		Log.d("xx", "attrColor=" + attrColor);
		Log.d("xx", "attrSize=" + attrSize);
		Log.d("xx", "ntsGravity=" + ntsGravity);

		TextView textView = new TextView(context);
		textView.setText(attrText);
		addView(textView);
		a.recycle();
	}
}
