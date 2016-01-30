package com.plugin.core.systemservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.plugin.content.PluginActivityInfo;
import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginProviderInfo;
import com.plugin.core.PluginLoader;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;
import com.plugin.util.ResourceUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cailiming on 16/1/15.
 */
public class AndroidAppIPackageManager extends MethodProxy {

    public static void installProxy(PackageManager manager) {
        LogUtil.d("安装PackageManagerProxy");
        Object androidAppIPackageManagerStubProxy = RefInvoker.getStaticFieldObject("android.app.ActivityThread", "sPackageManager");
        Object androidAppIPackageManagerStubProxyProxy = ProxyUtil.createProxy(androidAppIPackageManagerStubProxy, new AndroidAppIPackageManager());
        RefInvoker.setStaticOjbect("android.app.ActivityThread", "sPackageManager", androidAppIPackageManagerStubProxyProxy);
        RefInvoker.setFieldObject(manager, "android.app.ApplicationPackageManager", "mPM", androidAppIPackageManagerStubProxyProxy);
        LogUtil.d("安装完成");
    }

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
        sMethods.put("resolveActivity", new resolveActivity());
        sMethods.put("resolveActivityAsUser", new resolveActivityAsUser());
        sMethods.put("resolveService", new resolveService());
    }

    public static class getPackageInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            PluginDescriptor pd = PluginLoader.getPluginDescriptorByPluginId(args[0].toString());
            if (pd != null) {
                return PluginLoader.getApplicatoin().getPackageManager().getPackageArchiveInfo(pd.getInstalledPath(), (int) args[1]);
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getInstalledPackages extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeResult, Object invokeResult) {
            LogUtil.d("afterInvoke", method.getName());

            List<PackageInfo> result = (List<PackageInfo> )RefInvoker.invokeMethod(invokeResult, "android.content.pm.ParceledListSlice", "getList", (Class[])null, (Object[])null);

            Collection<PluginDescriptor> plugins = PluginLoader.getPlugins();
            if (plugins != null) {
                if (result == null) {
                    result = new ArrayList<PackageInfo>();
                }
                for(PluginDescriptor pluginDescriptor:plugins) {
                    PackageInfo info = PluginLoader.getApplicatoin().getPackageManager().getPackageArchiveInfo(pluginDescriptor.getInstalledPath(), (int) args[0]);
                    result.add(info);
                }
            }

            return invokeResult;
        }
    }

    public static class queryIntentActivities extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                List<ResolveInfo> result = new ArrayList<>();
                ResolveInfo info = new ResolveInfo();
                result.add(info);
                info.activityInfo = getActivityInfo(pluginDescriptor, classNames.get(0));
                return result;
            }
            return super.beforeInvoke(target, method, args);
        }

    }

    public static class getApplicationInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByPluginId(args[0].toString());
            if (pluginDescriptor != null) {
                return getApplicationInfo(pluginDescriptor);
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getActivityInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getActivityInfo(pluginDescriptor, className);
            }
            return super.beforeInvoke(target, method, args);
        }

    }

    public static class getReceiverInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getActivityInfo(pluginDescriptor, className);
            }
            return super.beforeInvoke(target, method, args);
        }

    }

    public static class getServiceInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                return getServiceInfo(pluginDescriptor, className);
            }

            return super.beforeInvoke(target, method, args);
        }
    }

    public static class getProviderInfo extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            String className = ((ComponentName)args[0]).getClassName();
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(className);
            if (pluginDescriptor != null) {
                PluginProviderInfo info = pluginDescriptor.getProviderInfos().get(className);
                ProviderInfo providerInfo = new ProviderInfo();
                providerInfo.name = info.getName();
                providerInfo.packageName = getPackageName(pluginDescriptor);
                providerInfo.icon = pluginDescriptor.getApplicationIcon();
                providerInfo.metaData = getMeta(pluginDescriptor.getMetaData());
                providerInfo.enabled = true;
                providerInfo.exported = info.isExported();
                providerInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
                providerInfo.authority = info.getAuthority();
                return providerInfo;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class queryIntentServices extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.SERVICE);
            if (classNames != null && classNames.size() > 0) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                List<ResolveInfo> result = new ArrayList<>();
                ResolveInfo info = new ResolveInfo();
                result.add(info);
                info.serviceInfo = getServiceInfo(pluginDescriptor, classNames.get(0));
                return result;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class resolveIntent extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                List<ResolveInfo> result = new ArrayList<>();
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                ResolveInfo info = new ResolveInfo();
                result.add(info);
                info.activityInfo = getActivityInfo(pluginDescriptor, classNames.get(0));
                return result;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class resolveService extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.SERVICE);
            if (classNames != null && classNames.size() > 0) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                ResolveInfo info = new ResolveInfo();
                info.serviceInfo = getServiceInfo(pluginDescriptor, classNames.get(0));
                return info;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    //public abstract ResolveInfo resolveActivity(Intent intent, int flags);
    public static class resolveActivity extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                ResolveInfo info = new ResolveInfo();
                info.activityInfo = getActivityInfo(pluginDescriptor, classNames.get(0));
                return info;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    //public abstract ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId);
    public static class resolveActivityAsUser extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.d("beforeInvoke", method.getName());
            ArrayList<String> classNames = PluginLoader.matchPlugin((Intent) args[0], PluginDescriptor.ACTIVITY);
            if (classNames != null && classNames.size() > 0) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(classNames.get(0));
                ResolveInfo info = new ResolveInfo();
                info.activityInfo = getActivityInfo(pluginDescriptor, classNames.get(0));
                return info;
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    private static Bundle getMeta(HashMap<String, String> map) {
        //TODO 可以缓存起来
        Bundle meta = new Bundle();
        if (map != null) {
            Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> item = entryIterator.next();
                //TODO 这里的类型还有很多其他情况
                if (TextUtils.isDigitsOnly(item.getValue())) {
                    meta.putInt(item.getKey(), Integer.valueOf(item.getValue()));
                } else {
                    meta.putString(item.getKey(), item.getValue());
                }
            }
        }
        return meta;
    }

    private static ApplicationInfo getApplicationInfo(PluginDescriptor pluginDescriptor) {
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = getPackageName(pluginDescriptor);
        info.metaData = getMeta(pluginDescriptor.getMetaData());
        info.name = pluginDescriptor.getApplicationName();
        info.className = pluginDescriptor.getApplicationName();
        info.enabled = true;
        info.processName = null;//需要时再添加
        info.sourceDir = pluginDescriptor.getInstalledPath();
        //info.uid == Process.myUid();
        info.publicSourceDir = pluginDescriptor.getInstalledPath();
        info.taskAffinity = null;//需要时再加上
        info.dataDir = null;//需要时再添加
        info.theme = pluginDescriptor.getApplicationTheme();
        info.flags = info.flags | ApplicationInfo.FLAG_HAS_CODE;
        return info;
    }

    private static ActivityInfo getActivityInfo(PluginDescriptor pluginDescriptor, String className) {
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.name = className;
        activityInfo.packageName = getPackageName(pluginDescriptor);
        activityInfo.icon = pluginDescriptor.getApplicationIcon();
        activityInfo.metaData = getMeta(pluginDescriptor.getMetaData());
        activityInfo.enabled = true;
        activityInfo.exported = false;
        activityInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
        activityInfo.taskAffinity = null;//需要时再加上
        //activityInfo.targetActivity =

        if (pluginDescriptor.getType(className) == PluginDescriptor.ACTIVITY) {
            PluginActivityInfo detail = pluginDescriptor.getActivityInfos().get(className);
            activityInfo.launchMode = Integer.valueOf(detail.getLaunchMode());
            activityInfo.theme = ResourceUtil.getResourceId(detail.getTheme());
            if (detail.getUiOptions() != null) {
                activityInfo.uiOptions = Integer.parseInt(detail.getUiOptions().replace("0x", ""), 16);
            }
        }
        return activityInfo;
    }

    private static ServiceInfo getServiceInfo(PluginDescriptor pluginDescriptor, String className) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.name = className;
        serviceInfo.packageName = getPackageName(pluginDescriptor);
        serviceInfo.icon = pluginDescriptor.getApplicationIcon();
        serviceInfo.metaData = getMeta(pluginDescriptor.getMetaData());
        serviceInfo.enabled = true;
        serviceInfo.exported = false;
        serviceInfo.applicationInfo = getApplicationInfo(pluginDescriptor);
        return serviceInfo;
    }

    private static String getPackageName(PluginDescriptor pluginDescriptor) {
        //这里的packageName可能需要使用宿主的packageName，
        return pluginDescriptor.getPackageName();
    }

}
