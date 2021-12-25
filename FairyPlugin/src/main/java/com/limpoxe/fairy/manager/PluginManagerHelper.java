package com.limpoxe.fairy.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginFilter;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

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

    /**
     * 此功能仅限开发测试时使用：
     * 为了在插件开发期间，方便插件的安装和卸载，监听系统广播。
     * 当收到插件的安装和卸载的系统广播时，自动将插件安装到宿主中，或自动从宿主中卸载
     * 其中，安装时由于框架默认限制了相同的版本好不重复安装，因此需要配合{@link FairyGlobal#isInstallationWithSameVersion()}使用
     * @param pluginPackageRegex
     */
    @Deprecated
    public static void autoInstallPackage(String[] pluginPackageRegex) {
        if (!FairyGlobal.isInited()) {
            return;
        }
        if (pluginPackageRegex == null || pluginPackageRegex.length == 0) {
            return;
        }
        try {
            //先把p当作非正则查询一次
            for (String p : pluginPackageRegex) {
                ApplicationInfo applicationInfo = FairyGlobal.getHostApplication().getPackageManager()
                        .getApplicationInfo(p, PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    LogUtil.d("发现已经安装到系统的插件包，触发安装插件", p, applicationInfo.sourceDir);
                    installPlugin(applicationInfo.sourceDir);
                }
            }
        } catch (Exception e) {
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        //intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        FairyGlobal.getHostApplication().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                    String pkgName = intent.getData().getSchemeSpecificPart();
                    for (String p : pluginPackageRegex) {
                        try {
                            if (Pattern.matches(p, pkgName)) {
                                ApplicationInfo applicationInfo = FairyGlobal.getHostApplication().getPackageManager()
                                        .getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
                                if (applicationInfo != null) {
                                    LogUtil.d("收到系统广播，触发安装插件", pkgName, applicationInfo.sourceDir);
                                    installPlugin(applicationInfo.sourceDir);
                                }
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                    String pkgName = intent.getData().getSchemeSpecificPart();
                    for (String p : pluginPackageRegex) {
                        try {
                            if (Pattern.matches(p, pkgName)) {
                                LogUtil.d("收到系统广播，触发卸载插件", pkgName);
                                PluginManagerHelper.remove(pkgName);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, intentFilter);
    }
}
