package com.plugin.core;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import com.plugin.content.LoadedPlugin;
import com.plugin.content.PluginDescriptor;
import com.plugin.core.manager.PluginManagerHelper;
import com.plugin.util.LogUtil;

import java.lang.reflect.Field;
import java.util.HashMap;

public class PluginThemeHelper {

	public static int getPluginThemeIdByName(String pluginId, String themeName) {
		PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
		if (pd != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginId);

			if (plugin != null) {
				return plugin.pluginResource.getIdentifier(themeName, "style", pd.getPackageName());
			}
		}
		return 0;
	}

	public static HashMap<String, Integer> getAllPluginThemes(String pluginId) {
		HashMap<String, Integer> themes = new HashMap<String, Integer>();
		PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
		if (pd != null) {
			//插件可能尚未初始化，确保使用前已经初始化
			LoadedPlugin pluing = PluginLauncher.instance().startPlugin(pluginId);

			try {
				Class pluginRstyle = pluing.pluginClassLoader.loadClass(pluginId + ".R$style");
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

		LayoutInflater layoutInflater = LayoutInflater.from(activity);
		if (layoutInflater.getFactory() == null) {
			if (!(activity.getBaseContext() instanceof PluginContextTheme)) {
				PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);
				if (pd != null) {

					//插件可能尚未初始化，确保使用前已经初始化
					LoadedPlugin pluing = PluginLauncher.instance().startPlugin(pluginId);

					//注入插件上下文和主题
					Context defaultContext = pluing.pluginContext;
					Context pluginContext = PluginLoader.getNewPluginComponentContext(defaultContext,
							((PluginBaseContextWrapper)activity.getBaseContext()).getBaseContext(), 0);
					PluginInjector.resetActivityContext(pluginContext, activity, themeResId);

				}
			}
		} else {
			LogUtil.e("启用了控件级插件的页面 不能使用换肤功能呢");
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