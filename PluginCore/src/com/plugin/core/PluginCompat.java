package com.plugin.core;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;

import android.content.Context;
import android.os.Build;

public class PluginCompat {
	
	/**
	 * “main_style_”是在pluglic.xml文件中定义的！
	 */
	private static final String MAIN_STYLE = "main_style_";
	
	/**
	 * 
	 * @param pluginContext
	 * @param resId    主题资源ID
	 * @param clazz
	 */
	public static void setTheme(Context pluginContext, int resId, @SuppressWarnings("rawtypes") Class clazz) {
		
		boolean isThemeInHostResouce = false;
		try {
			String themeEntryName = PluginLoader.getApplicatoin().getResources().getResourceEntryName(resId);
			LogUtil.d("themeEntryName", themeEntryName);
			if (themeEntryName != null && !themeEntryName.startsWith(MAIN_STYLE)) {
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
				PluginDescriptor pd = PluginLoader.getPluginDescriptorByClassName(clazz.getName());
				((PluginContextTheme)pluginContext).mResources = PluginCreator.createPluginResourceFor5(PluginLoader.getApplicatoin(), pd.getInstalledPath());
				((PluginContextTheme)pluginContext).mTheme = null;
				pluginContext.setTheme(resId);
			}
		}
	}
	
}
