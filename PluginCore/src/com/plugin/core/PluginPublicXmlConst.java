package com.plugin.core;

import android.util.SparseArray;

/**
 * Consts in public.xml
 */
public class PluginPublicXmlConst {
	public static final int public_static_final_host_attr = 0x7f31;
	public static final int public_static_final_host_drawable = 0x7f32;
	public static final int public_static_final_host_layout = 0x7f33;
	public static final int public_static_final_host_anim = 0x7f34;
	public static final int public_static_final_host_xml = 0x7f35;
	public static final int public_static_final_host_raw = 0x7f36;
	public static final int public_static_final_host_dimen = 0x7f37;
	public static final int public_static_final_host_string = 0x7f38;
	public static final int public_static_final_host_style = 0x7f39;
	public static final int public_static_final_host_color = 0x7f3a;
	public static final int public_static_final_host_id = 0x7f3b;
	public static final int public_static_final_host_bool = 0x7f3c;
	public static final int public_static_final_host_int = 0x7f3d;
	public static final int public_static_final_host_array = 0x7f3e;
	public static final int public_static_final_host_menu = 0x7f3f;
	
	public static SparseArray<String> resourceMap = new SparseArray<String>(16);
	
	static {
		resourceMap.put(public_static_final_host_attr, "attr");
		resourceMap.put(public_static_final_host_drawable, "drawable");
		resourceMap.put(public_static_final_host_layout, "layout");
		resourceMap.put(public_static_final_host_anim, "anim");
		resourceMap.put(public_static_final_host_xml, "xml");
		resourceMap.put(public_static_final_host_raw, "raw");
		resourceMap.put(public_static_final_host_dimen, "dimen");
		resourceMap.put(public_static_final_host_string, "string");
		resourceMap.put(public_static_final_host_style, "style");
		resourceMap.put(public_static_final_host_color, "color");
		resourceMap.put(public_static_final_host_id, "id");
		resourceMap.put(public_static_final_host_bool, "bool");
		resourceMap.put(public_static_final_host_int, "int");
		resourceMap.put(public_static_final_host_array, "array");
		resourceMap.put(public_static_final_host_menu, "menu");
	}
}
