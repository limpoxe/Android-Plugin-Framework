package com.example.pluginmain;

import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.plugin.core.PluginLoader;
import com.plugin.core.PluginDispatcher;

/**
 * 监听插件apk的安装广播， 并安装插件到宿主程序。
 * 
 * 并不是说插件apk需要安装到系统里面才可以使用，这只是为了方便调试，将已安装的apk当作插件apk的下载源.
 * 
 * 否则的话我们在调试的时候需要更新插件apk就比较麻烦
 * 
 * @author cailiming
 *
 */
public class PluginDebugHelper extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String pluginApkPath = getSource(context, intent);
		
		String defaultTarget = getDefaultTarget(context, intent);
		
		if (!TextUtils.isEmpty(pluginApkPath) && !TextUtils.isEmpty(defaultTarget)) {
			boolean success = PluginLoader.installPlugin(pluginApkPath);
			if (success) {
				PluginDispatcher.startFragmentWithSimpleActivity(context, defaultTarget);
			}
		}
		
	}
	
	private String getSource(Context context, Intent intent) {
		
		Log.v("PluginInstaller", intent.toUri(0));
		
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
		
			Log.e("PluginInstaller", "onReceive " + intent.getData().getSchemeSpecificPart());
			
			try {
				ApplicationInfo pinfo = context.getPackageManager().getApplicationInfo(
						intent.getData().getSchemeSpecificPart(), PackageManager.GET_META_DATA);

				return pinfo.sourceDir;

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)
				|| intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED)) {

		}
	
		return null;
	}
	
	private String getDefaultTarget(Context context, Intent intent) {
		
		Log.v("PluginInstaller", intent.toUri(0));
		
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
		
			Log.e("PluginInstaller", "onReceive " + intent.getData().getSchemeSpecificPart());
			
			try {
				ApplicationInfo pinfo = context.getPackageManager().getApplicationInfo(
						intent.getData().getSchemeSpecificPart(), PackageManager.GET_META_DATA);
				Iterator<String> iterator = pinfo.metaData.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = pinfo.metaData.getString(key);
					if (key.equals("launcher")) {
						return value;
					}
				}

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)
				|| intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED)) {

		}
	
		return null;
	}
}
