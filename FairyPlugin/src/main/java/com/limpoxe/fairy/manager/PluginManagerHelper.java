package com.limpoxe.fairy.manager;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginFilter;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.util.ArrayList;

/**
 * Created by cailiming on 16/3/11.
 * use PluginManager instead
 */
@Deprecated
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

    public static final int REMOVE_SUCCESS = 0;
    public static final int REMOVE_FAIL_PLUGIN_NOT_EXIST = 21;
    public static final int REMOVE_FAIL = 27;

    public static PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
        return PluginManagerProviderClient.queryByClass(clazzName);
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

        if (!PluginFilter.maybePlugin(pluginId)) {
            // 之所以有这判断, 是因为可能BinderProxyDelegate
            // 或者AndroidAppIPackageManager
            // 或者PluginBaseContextWrapper.createPackageContext
            // 中拦截了由系统发起的查询操作, 被拦截之后转到了这里
            // 所以在这做个快速判断.
            LogUtil.d("默认策略判定" + pluginId + "不是插件包名");
            return null;
        }

        return PluginManagerProviderClient.queryById(pluginId);
    }

    public static PluginDescriptor getPluginDescriptorByFragmentId(String clazzId) {
        return PluginManagerProviderClient.queryByFragment(clazzId);
    }

    public static int installPlugin(String srcFile) {
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

    public static int remove(String pluginId) {
        return PluginManagerProviderClient.remove(pluginId);
    }

    public static void stop(String pluginId) {
        PluginManagerProviderClient.stop(pluginId);
    }

    /**
     * 清除列表并不能清除已经加载到内存当中的class,因为class一旦加载后后无法卸载
     */
    public static void removeAll() {
        PluginManagerProviderClient.removeAll();
    }

    /**
     * 强行重启插件进程，杀掉所有运行中的插件
     */
    public static void rebootPluginProcess() {
        if (!ProcessUtil.isPluginProcess()) {//只在非插件进程调用才能做到重启，自己杀自己无法重启
            PluginManagerProviderClient.rebootPluginProcess();
        }
    }

}
