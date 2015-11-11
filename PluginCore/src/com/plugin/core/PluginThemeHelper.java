package com.plugin.core;

import android.app.Activity;
import android.content.Context;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.RefInvoker;

import java.lang.reflect.Field;
import java.util.HashMap;

public class PluginThemeHelper {

	public static int getPluginThemeIdByName(String pluginId, String themeName) {
		PluginDescriptor pd = PluginLoader.getPluginDescriptorByPluginId(pluginId);
		if (pd != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			PluginLoader.ensurePluginInited(pd);
			if (pd.getPluginContext() != null) {
				return pd.getPluginContext().getResources().getIdentifier(themeName, "style", pd.getPackageName());
			}
		}
		return 0;
	}

	public static HashMap<String, Integer> getAllPluginThemes(String pluginId) {
		HashMap<String, Integer> themes = new HashMap<String, Integer>();
		PluginDescriptor pd = PluginLoader.getPluginDescriptorByPluginId(pluginId);
		if (pd != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			PluginLoader.ensurePluginInited(pd);

			try {
				Class pluginRstyle = pd.getPluginClassLoader().loadClass(pluginId + ".R$style");
				if (pluginRstyle != null) {
					Field[] fields = pluginRstyle.getDeclaredFields();
					if (fields != null) {
						for (Field field :
								fields) {
							field.setAccessible(true);
							int themeResId = field.getInt(null);
							themes.put(field.getName(), themeResId);
						}
					}
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}


		return themes;
	}

	/**
	 * Used by host for skin
	 * 宿主程序使用插件主题
	 */
	public static void applyPluginTheme(Activity activity, String pluginId, int themeResId) {

		if (!(activity.getBaseContext() instanceof PluginContextTheme)) {
			PluginDescriptor pd = PluginLoader.getPluginDescriptorByPluginId(pluginId);
			if (pd != null) {

				//插件可能尚未初始化，确保使用前已经初始化
				PluginLoader.ensurePluginInited(pd);

				//注入插件上下文和主题
				Context defaultContext = pd.getPluginContext();
				Context pluginContext = PluginLoader.getNewPluginContext(defaultContext);
				PluginInjector.resetActivityContext(pluginContext, activity, themeResId);
			}
		}

	}

	/**
	 * Used by plugin for Theme
	 * 插件使用插件主题
	 */
	public static void setTheme(Context pluginContext, int resId) {
		if (pluginContext instanceof PluginContextTheme) {
			((PluginContextTheme)pluginContext).mTheme = null;
			pluginContext.setTheme(resId);
		}
	}

}