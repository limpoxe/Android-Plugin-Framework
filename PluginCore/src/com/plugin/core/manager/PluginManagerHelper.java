package com.plugin.core.manager;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by cailiming on 16/3/11.
 *
 */
public class PluginManagerHelper {

    //加个客户端进程的缓存，减少跨进程调用
    private static final HashMap<String, PluginDescriptor> localCache = new HashMap<String, PluginDescriptor>();

    private static ContentResolver getManagerProvider() {
        return PluginLoader.getApplication().getContentResolver();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {

        PluginDescriptor pluginDescriptor = localCache.get(clazzName);

        if (pluginDescriptor == null) {
            Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                    PluginManagerProvider.ACTION_QUERY_BY_CLASS_NAME, clazzName, null);
            if (bundle != null) {
                pluginDescriptor = (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_CLASS_NAME_RESULT);
                localCache.put(clazzName, pluginDescriptor);
            }
        }

        return pluginDescriptor;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("unchecked")
    public static Collection<PluginDescriptor> getPlugins() {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_ALL, null, null);
        if (bundle != null) {
            return (Collection<PluginDescriptor>)bundle.getSerializable(PluginManagerProvider.QUERY_ALL_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {

        PluginDescriptor pluginDescriptor = localCache.get(pluginId);

        if (pluginDescriptor == null) {
            Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                    PluginManagerProvider.ACTION_QUERY_BY_ID, pluginId, null);
            if (bundle != null) {
                pluginDescriptor = (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_ID_RESULT);
                localCache.put(pluginId, pluginDescriptor);
            }
        } else {
            LogUtil.d("取本端缓存", pluginDescriptor.getInstalledPath());
        }

        return pluginDescriptor;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int installPlugin(String srcFile) {
        clearLocalCache();
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_INSTALL, srcFile, null);
        if (bundle != null) {
            return bundle.getInt(PluginManagerProvider.INSTALL_RESULT);
        }
        return 7;//install-Fail
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static synchronized void remove(String pluginId) {
        clearLocalCache();
        getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE, pluginId, null);
    }

    /**
     * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static synchronized void removeAll() {
        clearLocalCache();
        getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE_ALL, null, null);
    }

    public static void clearLocalCache() {
        localCache.clear();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PluginDescriptor getPluginDescriptorByFragmentId(String clazzId) {

        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_FRAGMENT_ID, clazzId, null);
        if (bundle != null) {
            return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_FRAGMENT_ID_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubReceiver() {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_RECEIVER, null, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_RECEIVER_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubActivity(String pluginActivityClassName, int launchMode) {
        Bundle arg = new Bundle();
        arg.putInt("launchMode", launchMode);
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_ACTIVITY,
                pluginActivityClassName, arg);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_ACTIVITY_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean isExact(String name, int type) {
        Bundle arg = new Bundle();
        arg.putInt("type", type);
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_EXACT,
                name, arg);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_EXACT_RESULT);
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void unBindLaunchModeStubActivity(String activityName, String className) {
        Bundle arg = new Bundle();
        arg.putString("className", className);
        getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_ACTIVITY,
                activityName, arg);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String getBindedPluginServiceName(String stubServiceName) {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_GET_BINDED_SERVICE,
                stubServiceName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.GET_BINDED_SERVICE_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String bindStubService(String pluginServiceClassName) {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_SERVICE,
                pluginServiceClassName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_SERVICE_RESULT);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void unBindStubService(String pluginServiceName) {
        getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_SERVICE,
                pluginServiceName, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean isStubActivity(String className) {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_STUB_ACTIVITY,
                className, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_STUB_ACTIVITY_RESULT);
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String dumpServiceInfo() {
        Bundle bundle = getManagerProvider().call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_DUMP_SERVICE_INFO,
                null, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.DUMP_SERVICE_INFO_RESULT);
        }
        return null;
    }
}
