package com.plugin.core;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;

import java.lang.reflect.Constructor;

/**
 * 控件级插件的实现原理
 * 
 * @author cailiming
 * 
 */
public class PluginViewCreator implements LayoutInflater.Factory {

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {

		//可以在这里全局替换控件类型
		if ("TextView".equals(name)) {
			//return new CustomTextView();
		} else if ("ImageView".equals(name)) {
			//return new CustomImageView();
		}

		return createViewFromTag(context, name, attrs);

	}

	private View createViewFromTag(Context context, String name, AttributeSet attrs) {
		if (name.equals("pluginView")) {
			name = attrs.getAttributeValue(null, "class");
			String pluginId = attrs.getAttributeValue(null, "context");
			try {
				View view = createView(context, pluginId, name, attrs);
				if (view != null) {
					return view;
				}
			} catch (Exception e) {
			} finally {
			}

			View view = new View(context, attrs);
			view.setVisibility(View.GONE);
			return view;
		}

		return null;
	}

	private View createView(Context Context, String pluginId, String name, AttributeSet atts)
			throws ClassNotFoundException, InflateException {
		try {
			PluginDescriptor pd = PluginLoader.initPluginByPluginId(pluginId);
			if (pd != null) {
				Context baseContext = Context;
				if (!(baseContext instanceof PluginContextTheme)) {
					baseContext = ((ContextWrapper)baseContext).getBaseContext();
				}
				if (baseContext instanceof PluginContextTheme) {
					baseContext = ((PluginContextTheme) baseContext).getBaseContext();
				}
				Context pluginViewContext = PluginLoader.getNewPluginComponentContext(pd.getPluginContext(), baseContext, pd.getApplicationTheme());
				Class<? extends View> clazz = pluginViewContext.getClassLoader()
						.loadClass(name).asSubclass(View.class);

				Constructor<? extends View> constructor = clazz.getConstructor(new Class[] {
						Context.class, AttributeSet.class});
				constructor.setAccessible(true);
				return constructor.newInstance(new Object[]{pluginViewContext , atts});
			} else {
				LogUtil.e("未找到插件" + pluginId + "，请确认是否已安装");
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

}