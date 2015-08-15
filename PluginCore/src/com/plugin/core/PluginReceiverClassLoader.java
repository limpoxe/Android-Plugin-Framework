package com.plugin.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.plugin.core.ui.stub.PluginStubReceiver;
import com.plugin.util.LogUtil;

import dalvik.system.DexClassLoader;

/**
 * Just for receiver
 * 
 * @author Administrator
 * 
 */
public class PluginReceiverClassLoader extends DexClassLoader {

	/**
	 * unused
	 */
	private final BlockingQueue<String> mClassQueue = new LinkedBlockingQueue<String>();;

	/**
	 * unused
	 */
	public void offer(String className) {
		mClassQueue.offer(className);
	}

	public PluginReceiverClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	@Override
	protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

		// for Receiver
		if (className.startsWith(PluginStubReceiver.class.getName() + ".")) {
			String realName = className.replace(PluginStubReceiver.class.getName() + ".", "");
			LogUtil.d("PluginAppTrace", "className ", className, "target", realName);
			Class clazz = PluginLoader.loadPluginClassByName(realName);
			if (clazz != null) {
				return clazz;
			}
		}
		return super.loadClass(className, resolve);
	}

}
