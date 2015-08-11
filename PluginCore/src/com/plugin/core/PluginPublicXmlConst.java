package com.plugin.core;

import android.util.SparseArray;

public class PluginPublicXmlConst {
	public static final int main_attr = 0x7f01;
	public static final int main_drawable = 0x7f02;
	public static final int main_layout = 0x7f03;
	public static final int main_anim = 0x7f04;
	public static final int main_xml = 0x7f05;
	public static final int main_raw = 0x7f06;
	public static final int main_dimen = 0x7f07;
	public static final int main_string = 0x7f08;
	public static final int main_style = 0x7f09;
	public static final int main_color = 0x7f0a;
	public static final int main_id = 0x7f0b;
	public static final int main_bool = 0x7f0c;
	public static final int main_int = 0x7f0d;
	public static final int main_array = 0x7f0e;
	public static final int main_menu = 0x7f0f;
	
	public static SparseArray<String> resourceMap= new SparseArray<String>(16);
	
	static {
		resourceMap.put(main_attr, "attr");
		resourceMap.put(main_drawable, "drawable");
		resourceMap.put(main_layout, "layout");
		resourceMap.put(main_anim, "anim");
		resourceMap.put(main_xml, "xml");
		resourceMap.put(main_raw, "raw");
		resourceMap.put(main_dimen, "dimen");
		resourceMap.put(main_string, "string");
		resourceMap.put(main_style, "style");
		resourceMap.put(main_color, "color");
		resourceMap.put(main_id, "id");
		resourceMap.put(main_bool, "bool");
		resourceMap.put(main_int, "int");
		resourceMap.put(main_array, "array");
		resourceMap.put(main_menu, "menu");
	}
}
