package com.limpoxe.fairy.manager;

import android.os.Bundle;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.compat.CompatForContentProvider;

import java.util.ArrayList;

/**
 * Created by cailiming on 17/1/25.
 *
 */
public class PluginProviderClient {

    @SuppressWarnings("unchecked")
    public static ArrayList<PluginDescriptor> queryAll() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_ALL, null, null);

        ArrayList<PluginDescriptor> list = null;
        if (bundle != null) {
            list = (ArrayList<PluginDescriptor>)bundle.getSerializable(PluginManagerProvider.QUERY_ALL_RESULT);
        }
        //防止NPE
        if (list == null) {
            list = new ArrayList<PluginDescriptor>();
        }
        return list;
    }

    public static PluginDescriptor queryById(String pluginId) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_ID, pluginId, null);
        if (bundle != null) {
            return (PluginDescriptor) bundle.getSerializable(PluginManagerProvider.QUERY_BY_ID_RESULT);
        }
        return null;
    }

    public static PluginDescriptor queryByClass(String clazzName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_CLASS_NAME, clazzName, null);
        if (bundle != null) {
            return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_CLASS_NAME_RESULT);
        }
        return null;
    }

    public static PluginDescriptor queryByFragment(String clazzId) {

        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_FRAGMENT_ID, clazzId, null);
        if (bundle != null) {
            return (PluginDescriptor)bundle.getSerializable(PluginManagerProvider.QUERY_BY_FRAGMENT_ID_RESULT);
        }
        return null;
    }

    public static int install(String srcFile) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_INSTALL, srcFile, null);

        int result = 7;//install-Fail
        if (bundle != null) {
            result = bundle.getInt(PluginManagerProvider.INSTALL_RESULT);
        }
        return result;
    }

    public static synchronized int remove(String pluginId) {
        Bundle result = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE, pluginId, null);
        if (result != null) {
            return result.getInt(PluginManagerProvider.REMOVE_RESULT, PluginManagerHelper.REMOVE_FAIL);
        }
        return PluginManagerHelper.REMOVE_FAIL;
    }

    public static synchronized void removeAll() {
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE_ALL, null, null);
    }

    public static String bindStubReceiver(String className) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_RECEIVER, className, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_RECEIVER_RESULT);
        }
        return null;
    }

    public static String bindStubActivity(String pluginActivityClassName, int launchMode, String packageName, String themeId, String orientation) {
        Bundle arg = new Bundle();
        arg.putInt("launchMode", launchMode);
        arg.putString("packageName", packageName);
        arg.putString("themeId", themeId);
        if (orientation != null) {
            arg.putInt("orientation", Integer.valueOf(orientation));
        }
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
        //这里如果约定stub组件的名字以特定词开头可以省去provider调用，减少跨进程，提高效率
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
