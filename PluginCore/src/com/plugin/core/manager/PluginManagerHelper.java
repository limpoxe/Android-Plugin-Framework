package com.plugin.core.manager;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;

import java.util.Collection;

/**
 * Created by cailiming on 16/3/11.
 *
 */
public class PluginManagerHelper {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_QUERY_BY_CLASS_NAME, clazzName, null);
        return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_CLASS_NAME_RESULT);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("unchecked")
    public static Collection<PluginDescriptor> getPlugins() {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI,
                PluginManagerProvider.ACTION_QUERY_ALL, null, null);
        return (Collection<PluginDescriptor>)bundle.getSerializable(PluginManagerProvider.QUERY_ALL_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_QUERY_BY_ID, pluginId, null);
        return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_ID_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int installPlugin(String srcFile) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI,
                PluginManagerProvider.ACTION_INSTALL, srcFile, null);
        return bundle.getInt(PluginManagerProvider.INSTALL_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static synchronized void remove(String pluginId) {
        PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI,
                PluginManagerProvider.ACTION_REMOVE, pluginId, null);
    }

    /**
     * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static synchronized void removeAll() {
        PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI,
                PluginManagerProvider.ACTION_REMOVE_ALL, null, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByFragmentId(String clazzId) {

        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_QUERY_BY_FRAGMENT_ID, clazzId, null);
        return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_FRAGMENT_ID_RESULT);
    }
}
