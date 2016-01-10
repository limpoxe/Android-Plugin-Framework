package com.plugin.core.systemservice;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import com.plugin.content.PluginActivityInfo;
import com.plugin.content.PluginDescriptor;
import com.plugin.content.PluginIntentFilter;
import com.plugin.content.PluginProviderInfo;
import com.plugin.content.PluginReceiverIntent;
import com.plugin.core.PluginLoader;
import com.plugin.util.RefInvoker;
import com.plugin.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cailiming on 16/1/9.
 *
 * 只开放需要的api
 */
public class PluginPackageManager extends PackageManager {

    private PackageManager mBase;

    public PluginPackageManager(PackageManager base) {
        this.mBase = base;
    }

    ////////////////////////----Begin:常用API----////////////////////////
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        PluginDescriptor pd = PluginLoader.getPluginDescriptorByPluginId(packageName);
        if (pd != null) {
            return getPackageArchiveInfo(pd.getInstalledPath(), flags);
        }
        return mBase.getPackageInfo(packageName, flags);
    }

    @Override
    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        return mBase.getPackageArchiveInfo(archiveFilePath, flags);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        Collection<PluginDescriptor> plugins = PluginLoader.getPlugins();
        List<PackageInfo> result = new ArrayList<PackageInfo>();
        if (plugins != null) {
            for(PluginDescriptor pluginDescriptor:plugins) {
                try {
                    PackageInfo info = getPackageInfo(pluginDescriptor.getPackageName(), flags);
                    result.add(info);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
        return mBase.getInstalledPackages(flags);
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        // First see if the package has an INFO activity; the existence of
        // such an activity is implied to be the desired front-door for the
        // overall package (such as if it has multiple launcher entries).
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, 0);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        return intent;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        ArrayList<String> intentToResolve = PluginLoader.matchPlugin(intent, PluginDescriptor.ACTIVITY);
        if (intentToResolve != null && intentToResolve.size() > 0) {
            PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(intentToResolve.get(0));

            List<ResolveInfo> result = new ArrayList<>();
            ResolveInfo info = new ResolveInfo();
            result.add(info);
            info.resolvePackageName = pluginDescriptor.getPackageName();
            try {
                info.activityInfo = getActivityInfo(new ComponentName(pluginDescriptor.getPackageName(), intentToResolve.get(0)), flags);
            } catch (NameNotFoundException nnfe) {
            }

            return result;
        }
        return mBase.queryIntentActivities(intent, flags);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {

        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(component.getClassName());
        if (pluginDescriptor != null) {
            PluginActivityInfo pluginActivityInfo = pluginDescriptor.getActivityInfos().get(component.getClassName());
            ActivityInfo activityInfo = new ActivityInfo();
            activityInfo.name = pluginActivityInfo.getName();
            activityInfo.packageName = pluginDescriptor.getPackageName();
            activityInfo.launchMode = Integer.valueOf(pluginActivityInfo.getLaunchMode());
            activityInfo.theme = ResourceUtil.getResourceId(pluginActivityInfo.getTheme());
            if (pluginActivityInfo.getUiOptions() != null) {
                activityInfo.uiOptions = Integer.parseInt(pluginActivityInfo.getUiOptions().replace("0x", ""), 16);
            }
            activityInfo.icon = pluginDescriptor.getApplicationIcon();
            activityInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            activityInfo.enabled = true;
            activityInfo.exported = false;
            activityInfo.applicationInfo = getApplicationInfo(pluginDescriptor.getPackageName(), flags);
            activityInfo.taskAffinity = null;//需要时再加上
            return activityInfo;
        }

        return mBase.getActivityInfo(component, flags);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {

        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(component.getClassName());
        if (pluginDescriptor != null) {
            ArrayList<PluginIntentFilter> intentFilter = pluginDescriptor.getReceivers().get(component.getClassName());
            ActivityInfo activityInfo = new ActivityInfo();
            activityInfo.name = component.getClassName();
            activityInfo.packageName = pluginDescriptor.getPackageName();
            activityInfo.icon = pluginDescriptor.getApplicationIcon();
            activityInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            activityInfo.enabled = true;
            activityInfo.exported = false;
            activityInfo.applicationInfo = getApplicationInfo(pluginDescriptor.getPackageName(), flags);
            activityInfo.taskAffinity = null;//需要时再加上
            activityInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            return activityInfo;
        }

        return mBase.getReceiverInfo(component, flags);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(component.getClassName());
        if (pluginDescriptor != null) {
            ArrayList<PluginIntentFilter> intentFilter = pluginDescriptor.getServices().get(component.getClassName());
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.name = component.getClassName();
            serviceInfo.packageName = pluginDescriptor.getPackageName();
            serviceInfo.icon = pluginDescriptor.getApplicationIcon();
            serviceInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            serviceInfo.enabled = true;
            serviceInfo.exported = false;
            serviceInfo.applicationInfo = getApplicationInfo(pluginDescriptor.getPackageName(), flags);
            serviceInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            return serviceInfo;
        }

        return mBase.getServiceInfo(component, flags);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {

        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(component.getClassName());
        if (pluginDescriptor != null) {
            PluginProviderInfo info = pluginDescriptor.getProviderInfos().get(component.getClassName());
            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.name = info.getName();
            providerInfo.packageName = pluginDescriptor.getPackageName();
            providerInfo.icon = pluginDescriptor.getApplicationIcon();
            providerInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            providerInfo.enabled = true;
            providerInfo.exported = info.isExported();
            providerInfo.applicationInfo = getApplicationInfo(pluginDescriptor.getPackageName(), flags);
            providerInfo.metaData = getMeta(pluginDescriptor.getMetaData());
            providerInfo.authority = info.getAuthority();
            return providerInfo;
        }

        return mBase.getProviderInfo(component, flags);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(name);
        if (pluginDescriptor != null) {
            try {
                return getProviderInfo(new ComponentName(pluginDescriptor.getPackageName(), name), flags);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mBase.resolveContentProvider(name, flags);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        List<ResolveInfo> list = queryIntentActivities(intent, flags);
        if (list != null && list.size() == 1) {
            ResolveInfo info = list.get(0);
            if (PluginLoader.getPluginDescriptorByPluginId(info.resolvePackageName) != null) {
                return info;
            }
        }
        return mBase.resolveActivity(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        ArrayList<String> intentToResolve = PluginLoader.matchPlugin(intent, PluginDescriptor.BROADCAST);
        if (intentToResolve != null && intentToResolve.size() > 0) {

            List<ResolveInfo> result = new ArrayList<>();
            for(String item:intentToResolve) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(item);
                ResolveInfo info = new ResolveInfo();
                result.add(info);
                info.resolvePackageName = pluginDescriptor.getPackageName();
                try {
                    info.activityInfo = getReceiverInfo(new ComponentName(pluginDescriptor.getPackageName(), item), flags);
                } catch (NameNotFoundException nnfe) {
                }
            }
            return result;

        }
        return mBase.queryBroadcastReceivers(intent, flags);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        List<ResolveInfo> list = queryIntentServices(intent, flags);
        if (list != null && list.size() == 1) {
            ResolveInfo info = list.get(0);
            if (PluginLoader.getPluginDescriptorByPluginId(info.resolvePackageName) != null) {
                return info;
            }
        }
        return mBase.resolveService(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        ArrayList<String> intentToResolve = PluginLoader.matchPlugin(intent, PluginDescriptor.SERVICE);
        if (intentToResolve != null && intentToResolve.size() > 0) {

            List<ResolveInfo> result = new ArrayList<>();
            for(String item:intentToResolve) {
                PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(item);
                ResolveInfo info = new ResolveInfo();
                result.add(info);
                info.resolvePackageName = pluginDescriptor.getPackageName();
                try {
                    info.serviceInfo = getServiceInfo(new ComponentName(pluginDescriptor.getPackageName(), item), flags);
                } catch (NameNotFoundException nnfe) {
                }
            }

            return result;
        }
        return mBase.queryIntentServices(intent, flags);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByPluginId(packageName);
        if (pluginDescriptor != null) {
            ApplicationInfo info = new ApplicationInfo();

            info.packageName = pluginDescriptor.getPackageName();
            info.metaData = getMeta(pluginDescriptor.getMetaData());
            info.name = pluginDescriptor.getApplicationName();
            info.className = pluginDescriptor.getApplicationName();
            info.enabled = true;
            info.processName = null;//需要时再添加
            info.sourceDir = pluginDescriptor.getInstalledPath();
            info.taskAffinity = null;//需要时再加上
            info.dataDir = null;//需要时再添加
            info.theme = pluginDescriptor.getApplicationTheme();

            return info;
        }
        return mBase.getApplicationInfo(packageName, flags);
    }

    ////////////////////////----End:常用API----////////////////////////

    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return mBase.currentToCanonicalPackageNames(names);
    }

    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return mBase.canonicalToCurrentPackageNames(names);
    }

    @TargetApi(21)
    @Override
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return mBase.getLeanbackLaunchIntentForPackage(packageName);
    }

    @Override
    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return mBase.getPackageGids(packageName);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return mBase.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return mBase.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return mBase.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return mBase.getAllPermissionGroups(flags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        return mBase.getPackagesHoldingPermissions(permissions, flags);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return mBase.checkPermission(permName, pkgName);
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return mBase.addPermission(info);
    }

    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return mBase.addPermissionAsync(info);
    }

    @Override
    public void removePermission(String name) {
        mBase.removePermission(name);
    }

    @Override
    public int checkSignatures(int uid1, int uid2) {
        return mBase.checkSignatures(uid1, uid2);
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return mBase.checkSignatures(pkg1, pkg2);
    }

    @Override
    public String[] getPackagesForUid(int uid) {
        return mBase.getPackagesForUid(uid);
    }

    @Override
    public String getNameForUid(int uid) {
        return mBase.getNameForUid(uid);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return mBase.queryContentProviders(processName, uid, flags);
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return mBase.getInstalledApplications(flags);
    }

    @Override
    public String[] getSystemSharedLibraryNames() {
        return mBase.getSystemSharedLibraryNames();
    }

    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return mBase.getSystemAvailableFeatures();
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return mBase.hasSystemFeature(name);
    }


    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return mBase.getInstrumentationInfo(className, flags);
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return mBase.queryInstrumentation(targetPackage, flags);
    }

    @Override
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        return mBase.getDrawable(packageName, resid, appInfo);
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return mBase.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return mBase.queryIntentContentProviders(intent, flags);
    }

    @Override
    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return mBase.getActivityIcon(activityName);
    }

    @Override
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        return mBase.getActivityIcon(intent);
    }

    @TargetApi(20)
    @Override
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return mBase.getActivityBanner(activityName);
    }

    @TargetApi(20)
    @Override
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        return mBase.getActivityBanner(intent);
    }

    @Override
    public Drawable getDefaultActivityIcon() {
        return mBase.getDefaultActivityIcon();
    }


    @TargetApi(20)
    @Override
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return mBase.getApplicationBanner(info);
    }

    @TargetApi(20)
    @Override
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return mBase.getApplicationBanner(packageName);
    }

    @TargetApi(9)
    @Override
    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return mBase.getActivityLogo(activityName);
    }

    @TargetApi(9)
    @Override
    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        return mBase.getActivityLogo(intent);
    }

    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return mBase.getApplicationIcon(info);
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return mBase.getApplicationIcon(packageName);
    }

    @TargetApi(9)
    @Override
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return mBase.getApplicationLogo(info);
    }

    @TargetApi(9)
    @Override
    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return mBase.getApplicationLogo(packageName);
    }

    @TargetApi(21)
    @Override
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        return mBase.getUserBadgedIcon(icon, user);
    }

    @TargetApi(21)
    @Override
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return mBase.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
    }

    @TargetApi(21)
    @Override
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        return mBase.getUserBadgedLabel(label, user);
    }

    @Override
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return mBase.getText(packageName, resid, appInfo);
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return mBase.getXml(packageName, resid, appInfo);
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return mBase.getApplicationLabel(info);
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return mBase.getResourcesForActivity(activityName);
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return mBase.getResourcesForApplication(app);
    }

    @Override
    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return mBase.getResourcesForApplication(appPackageName);
    }

    @TargetApi(14)
    @Override
    public void verifyPendingInstall(int id, int verificationCode) {
        mBase.verifyPendingInstall(id, verificationCode);
    }

    @TargetApi(17)
    @Override
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        mBase.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    @TargetApi(14)
    @Override
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        mBase.setInstallerPackageName(targetPackage, installerPackageName);
    }

    @Override
    public String getInstallerPackageName(String packageName) {
        return mBase.getInstallerPackageName(packageName);
    }

    @Override
    @Deprecated
    public void addPackageToPreferred(String packageName) {
        mBase.addPackageToPreferred(packageName);
    }

    @Override
    @Deprecated
    public void removePackageFromPreferred(String packageName) {
        mBase.removePackageFromPreferred(packageName);
    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        return mBase.getPreferredPackages(flags);
    }

    @Override
    @Deprecated
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        mBase.addPreferredActivity(filter, match, set, activity);
    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {
        mBase.clearPackagePreferredActivities(packageName);
    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return mBase.getPreferredActivities(outFilters, outActivities, packageName);
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        mBase.setComponentEnabledSetting(componentName, newState, flags);
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return mBase.getComponentEnabledSetting(componentName);
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        mBase.setApplicationEnabledSetting(packageName, newState, flags);
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return mBase.getApplicationEnabledSetting(packageName);
    }

    @Override
    public boolean isSafeMode() {
        return mBase.isSafeMode();
    }

    @TargetApi(21)
    @Override
    public PackageInstaller getPackageInstaller() {
        return mBase.getPackageInstaller();
    }

    private Bundle getMeta(HashMap<String, String> map) {
        Bundle meta = new Bundle();
        if (map != null) {
            Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> item = entryIterator.next();
                if (TextUtils.isDigitsOnly(item.getValue())) {
                    meta.putString(item.getKey(), item.getValue());
                }
            }
        }
        return meta;
    }
}
