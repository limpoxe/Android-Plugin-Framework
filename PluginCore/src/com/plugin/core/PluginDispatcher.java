package com.plugin.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.plugin.core.ui.PluginNormalFragmentActivity;
import com.plugin.core.ui.PluginSpecFragmentActivity;
import com.plugin.core.ui.stub.PluginStubReceiver;
import com.plugin.core.ui.stub.PluginStubService;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

/**
 * 打开Fragment，service和receiver,activiy不需要
 * @author cailiming
 *
 */
public class PluginDispatcher {
	
	public static final String FRAGMENT_ID_IN_PLUGIN = "PluginDispatcher.fragmentId";
	public static final String ACTIVITY_ID_IN_PLUGIN = "PluginDispatcher.proxy.activity";
	public static final String RECEIVER_ID_IN_PLUGIN = "PluginDispatcher.receiver";
	
	/**
	 * 在普通的activity中展示插件中的fragment，
	 * 
	 * 因为fragment的宿主Activity是一个普通的activity，所以对目标fragment有特殊要求，
	 * 即fragment中所有需要使用context的地方，都是有PluginLoader.getPluginContext()来获取

	 * @param context
	 * @param target
	 */
	public static void startFragmentWithSimpleActivity(Context context, String targetId) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginNormalFragmentActivity.class);
		pluginActivity.putExtra(FRAGMENT_ID_IN_PLUGIN, targetId);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}
	
	/**
	 * 在重写过Context的activity中展示插件中的fragment，
	 * 
	 * 因为fragment的宿主Activity是重写过的，所以对目标fragment没有特殊要求，无需在fragment中包含任何插件相关的代码
	 * 
	 * 此重写过的activity同样可以展示通过包含PluginLoader.getPluginContext()获取context的fragment
	 * 
	 * @param context
	 * @param target
	 */
	public static void startFragmentWithBuildInActivity(Context context, String targetId) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginSpecFragmentActivity.class);
		pluginActivity.putExtra(FRAGMENT_ID_IN_PLUGIN, targetId);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}
	
	/**
	 * 放弃代理模式了。采用Activity免注册方式
	 */
	@Deprecated 
	public static void startProxyActivity(Context context, String targetId) {

//		Intent pluginActivity = new Intent();
//		pluginActivity.setClass(context, PluginProxyActivity.class);
//		pluginActivity.putExtra(ACTIVITY_ID_IN_PLUGIN, resloveTarget(targetId));
//		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(pluginActivity);
	
	}
	
	/**
	 * 可以通过重写基类的startServie方法，会比较方便
	 * @param context
	 * @param intent
	 */
	public static void startService(Context context, Intent intent) {
		if (hackClassLoadForServiceIfNeeded(intent)) {
			intent.setClass(context, PluginStubService.class);	
		}
		context.startService(intent);
	}
	
	/**
	 * 可以通过重写基类的sendBroadcast方法，会比较方便
	 * @param context
	 * @param intent
	 */
	public static void sendBroadcast(Context context, Intent intent) {
		if (hackClassLoadForReceiverIfNeeded(intent)) {
			Intent newIntent = new Intent();
			newIntent.setClass(context, PluginStubReceiver.class);
			newIntent.putExtra(RECEIVER_ID_IN_PLUGIN, intent);
			context.sendBroadcast(newIntent);
		} else {
			context.sendBroadcast(intent);
		}
	}
	
	/**
	 * 插件service免注册的主要实现原理
	 * @param intent
	 * @return
	 */
	 /*package*/ static boolean hackClassLoadForServiceIfNeeded(Intent intent) {
		String targetClassName = PluginLoader.isMatchPlugin(intent);
		if (targetClassName != null) {
			Object mLoadedApk = RefInvoker.getFieldObject(PluginLoader.getApplicatoin(), Application.class.getName(), "mLoadedApk");
			ClassLoader originalLoader = (ClassLoader) RefInvoker.getFieldObject(
					mLoadedApk, "android.app.LoadedApk", "mClassLoader");
			if (originalLoader instanceof PluginComponentLoader) {
				((PluginComponentLoader)originalLoader).offer(targetClassName);
			} else {
				PluginComponentLoader newLoader = new PluginComponentLoader("", PluginLoader.getApplicatoin().getCacheDir()
						.getAbsolutePath(), PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath(),
						originalLoader);
				newLoader.offer(targetClassName);
				RefInvoker.setFieldObject(mLoadedApk, "android.app.LoadedApk",
						"mClassLoader", newLoader);
			}
			return true;
		}
		return false;
	}
	
	 /*package*/ static boolean hackClassLoadForReceiverIfNeeded(Intent intent) {
		//如果在插件中发现了匹配intent的receiver项目，替换掉ClassLoader
		//不需要在这里记录目标className，className将在Intent中传递
		if (PluginLoader.isMatchPlugin(intent) != null) {
			Object mLoadedApk = RefInvoker.getFieldObject(PluginLoader.getApplicatoin(), Application.class.getName(), "mLoadedApk");
			ClassLoader originalLoader = (ClassLoader) RefInvoker.getFieldObject(
					mLoadedApk, "android.app.LoadedApk", "mClassLoader");
			if (!(originalLoader instanceof PluginComponentLoader)) {
				PluginComponentLoader newLoader = new PluginComponentLoader("", PluginLoader.getApplicatoin().getCacheDir()
						.getAbsolutePath(), PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath(),
						originalLoader);
				RefInvoker.setFieldObject(mLoadedApk, "android.app.LoadedApk",
						"mClassLoader", newLoader);
			}
			return true;
		}
		return false;
	}

	public static class PluginComponentLoader extends DexClassLoader {

		private final BlockingQueue<String> mServiceClassQueue = new LinkedBlockingQueue<String>();;
		
		public void offer(String className) {
			mServiceClassQueue.offer(className);
		}
		
		public PluginComponentLoader(String dexPath, String optimizedDirectory,
				String libraryPath, ClassLoader parent) {
			super(dexPath, optimizedDirectory, libraryPath, parent);
		}
		
		@Override
		protected Class<?> loadClass(String className, boolean resolve)
				throws ClassNotFoundException {
			
			if (className.equals(PluginStubService.class.getName())) {
				String target = mServiceClassQueue.poll();
				LogUtil.d("PluginAppTrace", "className ",className, "target", target);
				if (target != null) {
					@SuppressWarnings("rawtypes")
					Class clazz = PluginLoader.loadPluginClassByName(target);
					if (clazz != null) {
						return clazz;
					}
				} 
			} else if (className.startsWith(PluginStubReceiver.class.getName() + ".")) {
				String realName = className.replace(PluginStubReceiver.class.getName() + ".", "");
				LogUtil.d("PluginAppTrace", "className ",className, "target", realName);
				Class clazz = PluginLoader.loadPluginClassByName(realName);
				if (clazz != null) {
					return clazz;
				}
			}

			return super.loadClass(className, resolve);
		}
		
	}
	
}
