package com.limpoxe.fairy.manager;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cailiming on 16/3/11.
 *
 */
public class PluginManagerHelper {

    public static final int SUCCESS = 0;

    public static final int SRC_FILE_NOT_FOUND = 1;
    public static final int COPY_FILE_FAIL = 2;
    public static final int SIGNATURES_INVALIDATE = 3;
    public static final int VERIFY_SIGNATURES_FAIL = 4;
    public static final int PARSE_MANIFEST_FAIL = 5;
    public static final int FAIL_BECAUSE_SAME_VER_HAS_LOADED = 6;
    public static final int MIN_API_NOT_SUPPORTED = 8;
    public static final int INSTALL_FAIL = 7;
    public static final int HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN = 9;

    public static final int PLUGIN_NOT_EXIST = 21;
    public static final int REMOVE_FAIL = 27;

    //加个客户端进程的缓存，减少跨进程调用
    private static final HashMap<String, PluginDescriptor> localCache = new HashMap<String, PluginDescriptor>();

    public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {

        PluginDescriptor pluginDescriptor = localCache.get(clazzName);

        if (pluginDescriptor == null) {
            pluginDescriptor = PluginManagerProviderClient.queryByClass(clazzName);
            localCache.put(clazzName, pluginDescriptor);
        }

        return pluginDescriptor;
    }

    /**
     * 尽量减少调用此方法，特别是在插件比较多时，调用此方法会在进程间传递大量数据，
     * 一则影响性能，二则数据量可能会超出binder的数据传输上线而导致binder崩溃
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<PluginDescriptor> getPlugins() {
        return PluginManagerProviderClient.queryAll();
    }

    public static PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {

        if (pluginId.startsWith("com.android.")) {
            // 之所以有这判断, 是因为可能BinderProxyDelegate
            // 或者AndroidAppIPackageManager
            // 或者PluginBaseContextWrapper.createPackageContext
            // 中拦截了由系统发起的查询操作, 被拦截之后转到了这里
            // 所以在这做个快速判断.
            LogUtil.d("默认com.android.开头的包名不是插件");
            return null;
        }

        PluginDescriptor pluginDescriptor = localCache.get(pluginId);

        if (pluginDescriptor == null) {
            pluginDescriptor = PluginManagerProviderClient.queryById(pluginId);
            localCache.put(pluginId, pluginDescriptor);
        } else {
            LogUtil.v("取本端缓存", pluginDescriptor.getInstalledPath());
        }

        return pluginDescriptor;
    }

    public static PluginDescriptor getPluginDescriptorByFragmentId(String clazzId) {
        return PluginManagerProviderClient.queryByFragment(clazzId);
    }

    public static int installPlugin(String srcFile) {
        clearLocalCache();
        return PluginManagerProviderClient.install(srcFile);
    }

    public static boolean isInstalled(String pluginId) {
        PluginDescriptor pluginDescriptor = PluginManagerProviderClient.queryById(pluginId);
        return pluginDescriptor != null;
    }

    public static boolean isInstalled(String pluginId, String pluginVersion) {
        PluginDescriptor pluginDescriptor = PluginManagerProviderClient.queryById(pluginId);
        if (pluginDescriptor != null) {
            LogUtil.v("isInstalled", pluginId, pluginDescriptor.getVersion(), pluginVersion);
            return pluginDescriptor.getVersion().equals(pluginVersion);
        }
        return false;
    }

    public static boolean isRunning(String pluginId) {
        return PluginManagerProviderClient.isRunning(pluginId);
    }

    public static boolean wakeup(String pluginid) {
        return PluginManagerProviderClient.wakeup(pluginid);
    }

    public static synchronized int remove(String pluginId) {
        clearLocalCache();
        return PluginManagerProviderClient.remove(pluginId);
    }

    /**
     * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
     */
    public static synchronized void removeAll() {
        clearLocalCache();
        PluginManagerProviderClient.removeAll();
    }

    public static void clearLocalCache() {
        localCache.clear();
    }

}
