package com.limpoxe.fairy.core.proxy.systemservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.text.TextUtils;

import com.limpoxe.fairy.content.PluginActivityInfo;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.content.PluginProviderInfo;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginIntentResolver;
import com.limpoxe.fairy.core.android.HackActivityThread;
import com.limpoxe.fairy.core.android.HackApplicationPackageManager;
import com.limpoxe.fairy.core.android.HackParceledListSlice;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginManagerProvider;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppIPackageManager extends MethodProxy {

    static {
        sMethods.put("getInstalledPackages", new getInstalledPackages());
        sMethods.put("getPackageInfo", new getPackageInfo());
        sMethods.put("getApplicationInfo", new getApplicationInfo());
        sMethods.put("getActivityInfo", new getActivityInfo());
        sMethods.put("getReceiverInfo", new getReceiverInfo());
        sMethods.put("getServiceInfo", new getServiceInfo());
        sMethods.put("getProviderInfo", new getProviderInfo());
        sMethods.put("queryIntentActivities", new queryIntentActivities());
        sMethods.put("queryIntentServices", new queryIntentServices());
        sMethods.put("resolveIntent", new resolveIntent());
        sMethods.put("resolveService", new resolveService());
        sMethods.put("getComponentEnabledSetting", new getComponentEnabledSetting());
    }

    public static void installProxy(PackageManager manager) {
        LogUtil.d("安装PackageManagerProxy");
        Object androidAppIPackageManagerStubProxy = HackActivityThread.getPackageManager();
        Object androidAppIPackageManagerStubProxyProxy = ProxyUtil.createProxy(androidAppIPackageManagerStubProxy, new AndroidAppIPackageManager());
        HackActivityThread.setPackageManager(androidAppIPackageManagerStubProxyProxy);
        HackApplicationPackageManager hackApplicationPackageManager = new HackApplicationPackageManager(manager);
        hackApplicationPackageManager.setPM(androidAppIPackageManagerStubProxyProxy);
        LogUtil.d("安装完成");
    }

    public static class getPackageInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            String packageName = (String)args[0];
            LogUtil.v("beforeInvoke", method.getName(), packageName);
            if (!packageName.equals(FairyGlobal.getApplication().getPackageName())) {
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                if (pluginDescriptor != null) {
                    PackageInfo packageInfo = FairyGlobal.getApplication().getPackageManager().getPackageArchiveInfo(pluginDescriptor.getInstalledPath(), (int) args[1]);
                    if (packageInfo.applicationInfo != null) {
                        packageInfo.applicationInfo.sourceDir = pluginDescriptor.getInstalledPath();
                        packageInfo.applicationInfo.publicSourceDir = pluginDescriptor.getInstalledPath();
                    }
                    return packageInfo;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getInstalledPackages extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeResult, Object invokeResult) {
            LogUtil.v("afterInvoke", method.getName());

            if (Build.VERSION.SDK_INT >= 18) {//android4.3
                ArrayList<PluginDescriptor> plugins = PluginManagerHelper.getPlugins();
                if (plugins != null) {
                    List<PackageInfo> resultList = (List<PackageInfo>) new HackParceledListSlice(invokeResult).getList();
                    if (resultList != null) {
                        for(PluginDescriptor pluginDescriptor:plugins) {
                            PackageInfo packageInfo = FairyGlobal.getApplication().getPackageManager().getPackageArchiveInfo(pluginDescriptor.getInstalledPath(), (int) args[0]);
                            if (packageInfo != null && packageInfo.applicationInfo != null) {
                                packageInfo.applicationInfo.sourceDir = pluginDescriptor.getInstalledPath();
                                packageInfo.applicationInfo.publicSourceDir = pluginDescriptor.getInstalledPath();
                            } else {
                                //for trace
                                if (!TextUtils.isEmpty(pluginDescriptor.getInstalledPath())) {
                                    File file = new File(pluginDescriptor.getInstalledPath());
                                    LogUtil.e("getPackageArchiveInfo fail", file.exists(), file.canRead(), file.canWrite());
                                } else {
                                    LogUtil.e("getPackageArchiveInfo fail, path is empth");
                                }
                            }
                            resultList.add(packageInfo);
                        }
                    }
                }
            } else {
                //TODO android4.3以下对getInstalledPackages函数的hook
                LogUtil.e("not support this method getInstalledPackages  for api version " + Build.VERSION.SDK_INT);
            }
            return invokeResult;
        }
    }

    public static class queryIntentActivities extends MethodDelegate {
        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.v("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginIntentResolver.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                LogUtil.v("Plugin Activity Intent Match");
                ArrayList<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
                for(String className: classNames) {
                    PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                    if (pluginDescriptor != null) {
                        ResolveInfo info = new ResolveInfo();
                        info.activityInfo = getActivityInfo(pluginDescriptor, className);
                        info.labelRes = 0;//需要时再加上
                        resolveInfos.add(info);
                    } else {
                        LogUtil.v("PluginDescriptor Not Found");
                    }
                }
                if (resolveInfos.size() > 0) {
                    invokeResult = appendPluginResolveInfo(invokeResult, resolveInfos);
                }
            } else {
                LogUtil.v("It's not a plugin intent");
            }
            return invokeResult;
        }

    }

    public static class getApplicationInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            String packageName = (String)args[0];
            LogUtil.v("beforeInvoke", method.getName(), packageName);
            if (!packageName.equals(FairyGlobal.getApplication().getPackageName())) {
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                if (pluginDescriptor != null) {
                    return getApplicationInfo(pluginDescriptor);
                }
            } else {
                LogUtil.w("注意：使用了宿主包名：" + packageName);
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getActivityInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getActivityInfo(pluginDescriptor, className);
            }
            return super.beforeInvoke(target, method, args);
        }

    }

    public static class getReceiverInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getActivityInfo(pluginDescriptor, className);
            }
            return super.beforeInvoke(target, method, args);
        }

    }

    public static class getServiceInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getServiceInfo(pluginDescriptor, className);
            }

            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getProviderInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            if (!className.equals(PluginManagerProvider.class.getName())) {
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                if (pluginDescriptor != null) {
                    PluginProviderInfo info = pluginDescriptor.getProviderInfos().get(className);
                    ProviderInfo providerInfo = new ProviderInfo();
                    providerInfo.name = info.getName();
                    providerInfo.packageName = getPackageName(pluginDescriptor);
                    providerInfo.icon = pluginDescriptor.getApplicationIcon();
                    providerInfo.metaData = pluginDescriptor.getMetaData();
                    providerInfo.enabled = true;
                    providerInfo.exported = info.isExported();
                    providerInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
                    providerInfo.authority = info.getAuthority();
                    return providerInfo;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class queryIntentServices extends MethodDelegate {
        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
            LogUtil.v("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginIntentResolver.matchPlugin((Intent) args[0], PluginDescriptor.SERVICE);
            if (classNames != null && classNames.size() > 0) {
                LogUtil.v("Plugin Service Intent Match");
                ArrayList<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
                for(String className: classNames) {
                    PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                    if (pluginDescriptor != null) {
                        ResolveInfo info = new ResolveInfo();
                        info.serviceInfo = getServiceInfo(pluginDescriptor, className);
                        resolveInfos.add(info);
                    } else {
                        LogUtil.v("PluginDescriptor Not Found");
                    }
                }
                if (resolveInfos.size() > 0) {
                    invokeResult = appendPluginResolveInfo(invokeResult, resolveInfos);
                }
            } else {
                LogUtil.v("It's not a plugin intent");
            }
            return invokeResult;
        }
    }

    private static Object appendPluginResolveInfo(Object invokeResult, ArrayList<ResolveInfo> resolveInfos) {
        LogUtil.v("将插件组件信息插入结果集");
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return invokeResult;
        }

        if (Build.VERSION.SDK_INT <= 23) {
            if (invokeResult == null) {
                invokeResult = resolveInfos;
            } else {
                ((List)invokeResult).addAll(resolveInfos);
            }
        } else {
            // 高于7.0的版本应当返回的类型是 android.content.pm.ParceledListSlice
            if (invokeResult == null) {
                invokeResult = HackParceledListSlice.newParecledListSlice(resolveInfos);
            } else {
                List<ResolveInfo> resultList = (List<ResolveInfo>) new HackParceledListSlice(invokeResult).getList();
                resultList.addAll(resolveInfos);
            }
        }
        return invokeResult;
    }

    //ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId);
    public static class resolveIntent extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginIntentResolver.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                //TODO 只取第一个，忽略了多组件匹配到同一个Intent的情况
                String className = classNames.get(0);
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                ResolveInfo info = new ResolveInfo();
                info.activityInfo = getActivityInfo(pluginDescriptor, className);
                return info;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class resolveService extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginIntentResolver.matchPlugin((Intent) args[0], PluginDescriptor.SERVICE);
            if (classNames != null && classNames.size() > 0) {
                //TODO 只取第一个，忽略了多组件匹配到同一个Intent的情况
                String className = classNames.get(0);
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);
                ResolveInfo info = new ResolveInfo();
                info.serviceInfo = getServiceInfo(pluginDescriptor, className);
                return info;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getComponentEnabledSetting extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            Object arg0 = args[0];
            if (arg0 instanceof ComponentName) {
                ComponentName mComponentName = ((ComponentName) args[0]);

                LogUtil.v("beforeInvoke", method.getName(), mComponentName.getPackageName(), mComponentName.getClassName());

                if ("com.htc.android.htcsetupwizard".equalsIgnoreCase(mComponentName.getPackageName())) {
                    return PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                }
            } else {
                LogUtil.v("beforeInvoke", method.getName(), arg0);
            }

            return super.beforeInvoke(target, method, args);
        }
    }

    private static ApplicationInfo getApplicationInfo(PluginDescriptor pluginDescriptor) {
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = getPackageName(pluginDescriptor);
        info.metaData = pluginDescriptor.getMetaData();
        info.name = pluginDescriptor.getApplicationName();
        info.className = pluginDescriptor.getApplicationName();
        info.enabled = true;
        info.processName = null;//需要时再添加
        info.sourceDir = pluginDescriptor.getInstalledPath();
        info.dataDir = new File(pluginDescriptor.getInstalledPath()).getParent();
        //info.uid == Process.myUid();
        info.publicSourceDir = pluginDescriptor.getInstalledPath();
        info.taskAffinity = null;//需要时再加上
        info.theme = pluginDescriptor.getApplicationTheme();
        info.flags = info.flags | ApplicationInfo.FLAG_HAS_CODE;
        //需要时再添加
        //info.nativeLibraryDir = new File(pluginDescriptor.getInstalledPath()).getParentFile().getAbsolutePath() + "/lib";
        String targetSdkVersion = pluginDescriptor.getTargetSdkVersion();
        if (!TextUtils.isEmpty(targetSdkVersion)) {
            info.targetSdkVersion = Integer.valueOf(targetSdkVersion);
        } else {
            info.targetSdkVersion = FairyGlobal.getApplication().getApplicationInfo().targetSdkVersion;
        }
        return info;
    }

    private static ActivityInfo getActivityInfo(PluginDescriptor pluginDescriptor, String className) {
        LogUtil.v("getActivityInfo for plugin ", pluginDescriptor.getPackageName(), className);

        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.name = className;
        activityInfo.packageName = getPackageName(pluginDescriptor);
        activityInfo.icon = pluginDescriptor.getApplicationIcon();
        activityInfo.metaData = pluginDescriptor.getMetaData();
        activityInfo.enabled = true;
        activityInfo.exported = false;
        activityInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
        activityInfo.taskAffinity = null;//需要时再加上
        //activityInfo.targetActivity = //需要时再加上
        //activityInfo.softInputMode = //需要时再加上
        //activityInfo.screenOrientation = //需要时再加上

        if (pluginDescriptor.getType(className) == PluginDescriptor.ACTIVITY) {
            PluginActivityInfo detail = pluginDescriptor.getActivityInfos().get(className);
            activityInfo.launchMode = Integer.valueOf(detail.getLaunchMode());
            activityInfo.theme = ResourceUtil.parseResId(detail.getTheme());
            if (detail.getUiOptions() != null) {
                activityInfo.uiOptions = Integer.parseInt(detail.getUiOptions().replace("0x", ""), 16);
            }
            activityInfo.configChanges = detail.getConfigChanges();
        }
        return activityInfo;
    }

    private static ServiceInfo getServiceInfo(PluginDescriptor pluginDescriptor, String className) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.name = className;
        serviceInfo.packageName = getPackageName(pluginDescriptor);
        serviceInfo.icon = pluginDescriptor.getApplicationIcon();
        serviceInfo.metaData = pluginDescriptor.getMetaData();
        serviceInfo.enabled = true;
        serviceInfo.exported = false;
        //加上插件中配置进程名称后缀
        String process = pluginDescriptor.getServiceInfos().get(className);
        if (process == null) {
            serviceInfo.processName = FairyGlobal.getApplication().getPackageName();
        } else if (process.startsWith(":")) {
            serviceInfo.processName = FairyGlobal.getApplication().getPackageName() + process;
        } else {
            serviceInfo.processName = process;
        }
        serviceInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
        return serviceInfo;
    }

    private static String getPackageName(PluginDescriptor pluginDescriptor) {
        //这里要使用插件包名
        return pluginDescriptor.getPackageName();
    }

}
