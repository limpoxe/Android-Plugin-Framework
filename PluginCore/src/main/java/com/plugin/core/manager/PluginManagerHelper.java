package com.plugin.core.manager;

import android.os.Bundle;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.compat.CompatForContentProvider;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by cailiming on 16/3/11.
 *
 */
public class PluginManagerHelper {

    //加个客户端进程的缓存，减少跨进程调用
    private static final HashMap<String, PluginDescriptor> localCache = new HashMap<String, PluginDescriptor>();

    public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {

        PluginDescriptor pluginDescriptor = localCache.get(clazzName);

        if (pluginDescriptor == null) {
            Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                    PluginManagerProvider.ACTION_QUERY_BY_CLASS_NAME, clazzName, null);
            if (bundle != null) {
                pluginDescriptor = (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_CLASS_NAME_RESULT);
                localCache.put(clazzName, pluginDescriptor);
            }
        }

        return pluginDescriptor;
    }

    @SuppressWarnings("unchecked")
    public static Collection<PluginDescriptor> getPlugins() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_ALL, null, null);

        Collection<PluginDescriptor> list = null;
        if (bundle != null) {
            list = (Collection<PluginDescriptor>)bundle.getSerializable(PluginManagerProvider.QUERY_ALL_RESULT);
        }
        //防止NPE
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {

        if ("com.android.webview".equals(pluginId)) {
            // 之所以有这判断, 是因为可能BinderProxyDelegate
            // 或者AndroidAppIPackageManager
            // 或者PluginBaseContextWrapper.createPackageContext
            // 中拦截了由系统发起的查询操作, 被拦截之后转到了这里
            // 所有在这做个快速判断.
            return null;
        }

        PluginDescriptor pluginDescriptor = localCache.get(pluginId);

        if (pluginDescriptor == null) {
            Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
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

    public static int installPlugin(String srcFile) {
        clearLocalCache();
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_INSTALL, srcFile, null);

        int result = 7;//install-Fail
        if (bundle != null) {
            result = bundle.getInt(PluginManagerProvider.INSTALL_RESULT);
        }
        if (result == 0) {//success

            LocalServiceManager.unRegistAll();

            Iterator<PluginDescriptor> itr = PluginManagerHelper.getPlugins().iterator();
            while (itr.hasNext()) {
                PluginDescriptor plugin = itr.next();
                LocalServiceManager.registerService(plugin);
            }
        }
        return result;
    }

    public static synchronized void remove(String pluginId) {
        clearLocalCache();
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE, pluginId, null);
    }

    /**
     * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
     */
    public static synchronized void removeAll() {
        clearLocalCache();
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE_ALL, null, null);
    }

    public static void clearLocalCache() {
        localCache.clear();
    }

    public static PluginDescriptor getPluginDescriptorByFragmentId(String clazzId) {

        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_FRAGMENT_ID, clazzId, null);
        if (bundle != null) {
            return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_FRAGMENT_ID_RESULT);
        }
        return null;
    }

    public static String bindStubReceiver() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_RECEIVER, null, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_RECEIVER_RESULT);
        }
        return null;
    }

    public static String bindStubActivity(String pluginActivityClassName, int launchMode) {
        Bundle arg = new Bundle();
        arg.putInt("launchMode", launchMode);
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_ACTIVITY,
                pluginActivityClassName, arg);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_ACTIVITY_RESULT);
        }
        return null;
    }

    public static boolean isExact(String name, int type) {
        Bundle arg = new Bundle();
        arg.putInt("type", type);
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_EXACT,
                name, arg);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_EXACT_RESULT);
        }
        return false;
    }

    public static void unBindLaunchModeStubActivity(String activityName, String className) {
        Bundle arg = new Bundle();
        arg.putString("className", className);
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_ACTIVITY,
                activityName, arg);
    }

    public static String getBindedPluginServiceName(String stubServiceName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_GET_BINDED_SERVICE,
                stubServiceName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.GET_BINDED_SERVICE_RESULT);
        }
        return null;
    }

    public static String bindStubService(String pluginServiceClassName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_SERVICE,
                pluginServiceClassName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_SERVICE_RESULT);
        }
        return null;
    }

    public static void unBindStubService(String pluginServiceName) {
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_SERVICE,
                pluginServiceName, null);
    }

    public static boolean isStub(String className) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_STUB,
                className, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_STUB_RESULT);
        }
        return false;
    }

    public static String dumpServiceInfo() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_DUMP_SERVICE_INFO,
                null, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.DUMP_SERVICE_INFO_RESULT);
        }
        return null;
    }
}
