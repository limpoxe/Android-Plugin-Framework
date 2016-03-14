package com.plugin.core.manager;

import android.annotation.TargetApi;
import android.content.Intent;
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubReceiver() {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_BIND_RECEIVER, null, null);
        return bundle.getString(PluginManagerProvider.BIND_RECEIVER_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubActivity(String pluginActivityClassName, int launchMode) {
        Bundle arg = new Bundle();
        arg.putInt("launchMode", launchMode);
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_BIND_ACTIVITY,
                pluginActivityClassName, arg);
        return bundle.getString(PluginManagerProvider.BIND_ACTIVITY_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean isExact(String name, int type) {
        Bundle arg = new Bundle();
        arg.putInt("type", type);
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_IS_EXACT,
                name, arg);
        return bundle.getBoolean(PluginManagerProvider.IS_EXACT_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void unBindLaunchModeStubActivity(String activityName, String className) {
        Bundle arg = new Bundle();
        arg.putString("className", className);
        PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_UNBIND_ACTIVITY,
                activityName, arg);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String getBindedPluginServiceName(String stubServiceName) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_GET_BINDED_SERVICE,
                stubServiceName, null);
        return bundle.getString(PluginManagerProvider.GET_BINDED_SERVICE_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubService(String pluginServiceClassName) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_BIND_SERVICE,
                pluginServiceClassName, null);
        return bundle.getString(PluginManagerProvider.BIND_SERVICE_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void unBindStubService(String pluginServiceName) {
        PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_UNBIND_SERVICE,
                pluginServiceName, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean isStubActivity(String className) {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_IS_STUB_ACTIVITY,
                className, null);
        return bundle.getBoolean(PluginManagerProvider.IS_STUB_ACTIVITY_RESULT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String dumpServiceInfo() {
        Bundle bundle = PluginLoader.getApplicatoin().getContentResolver().call(PluginManagerProvider.CONTENT_URI, PluginManagerProvider.ACTION_DUMP_SERVICE_INFO,
                null, null);
        return bundle.getString(PluginManagerProvider.DUMP_SERVICE_INFO_RESULT);
    }
}
