package com.plugin.core;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.widget.RemoteViews;

import com.plugin.util.FileUtil;
import com.plugin.util.RefInvoker;

import java.io.File;

public class PluginRemoteViewHelper {

	public static RemoteViews createRemoteViews(int pluginNotificationLayout, String notificationResPath, String pluginId) {

		return createRemoteViews(pluginNotificationLayout, getNotificationResourcePath(pluginId, notificationResPath));
	}

	public static RemoteViews createRemoteViews(int pluginNotificationLayout, String notificationResPath) {

		RemoteViews remoteViews = new RemoteViews(PluginLoader.getApplicatoin().getPackageName(),
				pluginNotificationLayout);

		if (Build.VERSION.SDK_INT >=21) {

			ApplicationInfo applicationInfo = (ApplicationInfo) RefInvoker.getFieldObject(remoteViews, RemoteViews.class.getName(), "mApplication");
			if (applicationInfo != null) {
				ApplicationInfo newInfo = new ApplicationInfo();//重新构造一个，而不是修改原本的
				newInfo.packageName = applicationInfo.packageName;
				newInfo.sourceDir = applicationInfo.sourceDir;
				newInfo.dataDir = applicationInfo.dataDir;
				newInfo.publicSourceDir = notificationResPath;//要确保worldReadablePath这个路径可以被SystemUI应用读取，
				RefInvoker.setFieldObject(remoteViews, RemoteViews.class.getName(), "mApplication", newInfo);
			}
		}
		return remoteViews;
	}

	private static String getNotificationResourcePath(String pluginId, String worldReadablePath) {

		String pluginSrc = PluginLoader.getPluginDescriptorByPluginId(pluginId).getInstalledPath();

		File worldReadableFile = new File(worldReadablePath);

		if (FileUtil.copyFile(pluginSrc, worldReadableFile.getAbsolutePath())) {
			return worldReadableFile.getAbsolutePath();
		} else {
			//should not happen
			return pluginSrc;
		}
	}

}