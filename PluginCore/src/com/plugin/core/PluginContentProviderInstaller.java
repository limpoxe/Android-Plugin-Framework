package com.plugin.core;

import android.content.Context;
import android.content.pm.ProviderInfo;

import com.plugin.content.PluginProviderInfo;
import com.plugin.util.ClassLoaderUtil;
import com.plugin.util.RefInvoker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginContentProviderInstaller {

	public static void installContentProviders(Context context, Collection<PluginProviderInfo> pluginProviderInfos) {
		if (PluginLoader.getActivityThread() != null) {
			ClassLoaderUtil.hackClassLoaderIfNeeded();
			List<ProviderInfo> providers = new ArrayList<ProviderInfo>();
			for (PluginProviderInfo pluginProviderInfo : pluginProviderInfos) {
				ProviderInfo p = new ProviderInfo();
				p.name = pluginProviderInfo.getName();
				p.authority = pluginProviderInfo.getAuthority();
				p.applicationInfo = context.getApplicationInfo();
				p.exported = pluginProviderInfo.isExported();
				p.packageName = context.getApplicationInfo().packageName;
				providers.add(p);
			}
			RefInvoker.invokeMethod(PluginLoader.getActivityThread(),
					"android.app.ActivityThread", "installContentProviders",
					new Class[]{Context.class, List.class}, new Object[]{context, providers});
		}
	}
}

