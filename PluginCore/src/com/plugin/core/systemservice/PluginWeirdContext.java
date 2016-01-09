package com.plugin.core.systemservice;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginBaseContextWrapper;
import com.plugin.core.PluginContextTheme;
import com.plugin.core.PluginLoader;

/**
 * Created by cailiming on 16/1/9.
 *
 * 如果你不知道、不确定自己在干什么，请慎用此API！！！
 * 如果你不知道、不确定自己在干什么，请慎用此API！！！
 *
 */
public class PluginWeirdContext extends PluginContextTheme {

    public PluginWeirdContext(PluginDescriptor pluginDescriptor,
                              Context base, Resources resources,
                              ClassLoader classLoader) {
        super(pluginDescriptor, base, resources, classLoader);
    }

    @Override
    public String getPackageName() {
        return mPluginDescriptor.getPackageName();
    }

    public PackageManager getPackageManager() {
        PackageManager pm = super.getPackageManager();
        return new PluginPackageManager(pm);
    }

    @Override
    public Object getSystemService(String name) {
        Object service = getBaseContext().getSystemService(name);
        if (service != null) {
            SystemServiceHook.replaceContext(service, ((ContextWrapper)this.getBaseContext()));
            return service;
        }
        return super.getSystemService(name);
    }
}
