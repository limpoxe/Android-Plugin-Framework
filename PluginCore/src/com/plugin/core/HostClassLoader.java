package com.plugin.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.plugin.util.LogUtil;

import dalvik.system.DexClassLoader;

/**
 * 为了支持Receiver和ContentProvider，增加此类。
 * 
 * @author Administrator
 * 
 */
public class HostClassLoader extends DexClassLoader {

	public HostClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	@Override
	public String findLibrary(String name) {
		LogUtil.d("findLibrary", name);
		return super.findLibrary(name);
	}

	@Override
	protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

		//Just for Receiver and service

		if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_SERVICE)) {

			return PluginShadowService.class;

		} else if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_RECEIVER)) {

			String realName = className.replace(PluginIntentResolver.CLASS_PREFIX_RECEIVER, "");

			LogUtil.d("className ", className, "target", realName);

			Class clazz = PluginLoader.loadPluginClassByName(realName);
			if (clazz != null) {
				return clazz;
			} else {
				LogUtil.e("到了这里说明出bug了,这里做个容错处理, 避免出现classnotfound");
				return new BroadcastReceiver(){@Override public void onReceive(Context context, Intent intent) {}}.getClass();
			}
		}

		return super.loadClass(className, resolve);
	}

}
