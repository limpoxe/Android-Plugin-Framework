package com.plugin.core;

import com.plugin.content.PluginDescriptor;

import android.content.Context;

public class PluginCompat {
	
	/**
	 * for eclipse with public.xml
	 * “main_style_”是在pluglic.xml文件中定义的！
	 */
	//private static final String MAIN_STYLE = "main_style_";
	
	/**
	 * 
	 * @param pluginContext
	 * @param resId    主题资源ID
	 * @param clazz
	 */
	public static void setTheme(Context pluginContext, int resId, @SuppressWarnings("rawtypes") Class clazz) {
		setTheme(pluginContext, resId, PluginLoader.getPluginDescriptorByClassName(clazz.getName()));
	}

	public static void setTheme(Context pluginContext, int resId, PluginDescriptor pd) {

		/**
		boolean isThemeInHostResouce = false;
		try {
			//如果使用public.xml,采用下面的判断方式
			//String themeEntryName = PluginLoader.getApplicatoin().getResources().getResourceEntryName(resId);
			//LogUtil.d("themeEntryName", themeEntryName);
			//if (themeEntryName != null && !themeEntryName.startsWith(MAIN_STYLE)) {
			//	isThemeInHostResouce = true;
			//}

			//如果使用openatlasExtentoin，采用下面的判断方式
			if (resId >> 24 != 0x7f) {
				isThemeInHostResouce = true;
			}
		} catch (Exception e) {
		}

		if (isThemeInHostResouce) {
			//使用主程序资源Id不需要区分版本
			pluginContext.setTheme(resId);
		} else {
			//使用插件程序主题资源Id的时候需要区分版本
			if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT <= 20) {
				pluginContext.setTheme(resId);
			} else {
				((PluginContextTheme)pluginContext).mResources = PluginCreator.createPluginResourceFor5(PluginLoader.getApplicatoin(), pd.getInstalledPath());
				((PluginContextTheme)pluginContext).mTheme = null;
				pluginContext.setTheme(resId);
			}
		}*/

		((PluginContextTheme)pluginContext).mTheme = null;
		pluginContext.setTheme(resId);
	}
	
}
