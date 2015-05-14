package com.plugin.core.ui;

import com.plugin.core.PluginLoader;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class PluginDispatcher {
	
	/**
	 * 在普通的activity中展示插件中的fragment，
	 * 
	 * 因为fragment的宿主Activity是一个普通的activity，所以对目标fragment有特殊要求，
	 * 即fragment中所有需要使用context的地方，都是有PluginLoader.getPluginContext()来获取

	 * @param context
	 * @param target
	 */
	public static void startFragmentWithSimpleActivity(Context context, String target) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginNormalDisplayer.class);
		pluginActivity.putExtra("classId", resloveTarget(target));
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
	public static void startFragmentWithBuildInActivity(Context context, String target) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginSpecDisplayer.class);
		pluginActivity.putExtra("classId", resloveTarget(target));
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}
	
	/**
	 * 显示插件中的activity
	 * 
	 *  因为目标activity的宿主Activity是重写过的，所以对目标activity没有特色要求
	 * 
	 * @param context
	 * @param target
	 */
	public static void startProxyActivity(Context context, String target) {

		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginProxyActivity.class);
		pluginActivity.putExtra("classId", resloveTarget(target));
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	
	}
	
	/**
	 * 显示插件中的activity
	 * 
	 * 打开stubActivity实际上会打开插件中test5对应的activity	 * 
	 * @param context
	 * @param target
	 */
	public static void startRealActivity(Context context, String target) {
		
		//替换成可以加载 插件元素test5 的classLoader
		replaceClassLoader(target);
		
		Intent pluginActivity = new Intent();
		pluginActivity.setClass(context, PluginStubActivity.class);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	
	}
	
	private static String resloveTarget(String target) {
		//TODO target到classId的映射
		return target;
	}
	
	public static class PluginComponentLoader extends DexClassLoader {

		private String currentId;
		
		public void setCurrentId(String classId) {
			this.currentId = classId;
		}
		
		public PluginComponentLoader(String dexPath, String optimizedDirectory,
				String libraryPath, ClassLoader parent) {
			super(dexPath, optimizedDirectory, libraryPath, parent);
		}
		
		@Override
		protected Class<?> loadClass(String className, boolean resolve)
				throws ClassNotFoundException {
			
			if (currentId != null && className.equals(PluginStubActivity.class.getName())) {
				@SuppressWarnings("rawtypes")
				Class clazz = PluginLoader.loadPluginClassById(currentId);
				currentId = null;
				if (clazz != null) {
					return clazz;
				}
			}
			
			return super.loadClass(className, resolve);
		}
		
	}
	
	private static void replaceClassLoader(String target) {

		Object mLoadedApk = RefInvoker.getFieldObject(PluginLoader.getApplicatoin(), Application.class.getName(), "mLoadedApk");
		ClassLoader originalLoader = (ClassLoader) RefInvoker.getFieldObject(
				mLoadedApk, "android.app.LoadedApk", "mClassLoader");
		
		if (originalLoader instanceof PluginComponentLoader) {
			((PluginComponentLoader)originalLoader).setCurrentId(target);
		} else {
			PluginComponentLoader dLoader = new PluginComponentLoader("", PluginLoader.getApplicatoin().getCacheDir()
					.getAbsolutePath(), PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath(),
					originalLoader);
			dLoader.setCurrentId(target);
			RefInvoker.setFieldObject(mLoadedApk, "android.app.LoadedApk",
					"mClassLoader", dLoader);
		}
	}
	
}
