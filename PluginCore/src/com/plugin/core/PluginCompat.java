package com.plugin.core;

import android.content.Context;
import android.os.Build;

public class PluginCompat {
	
	/**
	 * 
	 * @param pluginContext
	 * @param resId    宿主程序中定义的主题资源ID
	 * @param clazz
	 */
	public static void applyHostTheme(Context pluginContext, int resId, @SuppressWarnings("rawtypes") Class clazz) {
		//使用主程序资源Id不需要区分版本
		pluginContext.setTheme(resId);
	}
	
	/**
	 * 
	 * @param pluginContext
	 * @param resId  插件程序中定义的主题资源ID
	 * @param clazz
	 */
	public static void applyPluginTheme(Context pluginContext, int resId, @SuppressWarnings("rawtypes") Class clazz) {
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
