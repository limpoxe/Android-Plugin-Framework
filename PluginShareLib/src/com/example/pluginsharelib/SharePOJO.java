package com.example.pluginsharelib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.io.Serializable;

/**
 * 仅仅用来测试插件程序中Intent是否可以使用宿主程序中的VO
 * @author cailiming
 *
 */
public class SharePOJO implements Serializable {

	public SharePOJO(String name) {
		this.name = name;
	}

	public String name;
}
