package com.limpoxe.fairy.core;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;

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

			String pluginId = attrs.getAttributeValue(null, "context");
			String viewClassName = attrs.getAttributeValue(null, "class");

			LogUtil.v("创建插件view", pluginId, viewClassName);

			try {
				View view = createView(context, pluginId, viewClassName, attrs);
				if (view != null) {
					return view;
				}
			} catch (Exception e) {
				LogUtil.printException("PluginViewCreator.createViewFromTag", e);
			} finally {
			}

			View view = new View(context, attrs);
			view.setVisibility(View.GONE);
			return view;
		}

		return null;
	}

	private View createView(Context Context, String pluginId, String viewClassName, AttributeSet atts)
			throws ClassNotFoundException, InflateException {
		try {
			PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pluginId);

			if (pluginDescriptor != null) {

				//插件可能尚未初始化，确保使用前已经初始化
				LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginDescriptor);

				Context baseContext = Context;
				if (!(baseContext instanceof PluginContextTheme)) {
					baseContext = ((ContextWrapper)baseContext).getBaseContext();
				}
				if (baseContext instanceof PluginContextTheme) {
					baseContext = ((PluginContextTheme) baseContext).getBaseContext();
				}
				Context pluginViewContext = PluginCreator.createNewPluginComponentContext(plugin.pluginContext, baseContext, pluginDescriptor.getApplicationTheme());
				Class<? extends View> clazz = pluginViewContext.getClassLoader()
						.loadClass(viewClassName).asSubclass(View.class);

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