package com.plugin.core.systemservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;

import com.plugin.util.RefInvoker;

import java.util.List;

/**
 * Created by cailiming on 16/1/9.
 *
 * base on android4.4_r1
 *
 */
public class PluginPackageManager extends PackageManager {

    private int mUseId;//mContext.getUserId()
    private PackageManager mPM;

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags)
            throws NameNotFoundException {
        return mPM.getPackageInfo(packageName, flags);
    }

    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return mPM.currentToCanonicalPackageNames(names);
    }

    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return mPM.canonicalToCurrentPackageNames(names);
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
    public int[] getPackageGids(String packageName)
            throws NameNotFoundException {
        return mPM.getPackageGids(packageName);
    }

    public int getPackageUid(String packageName, int userHandle)
            throws NameNotFoundException {
        Integer result = (Integer)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getPackageUid", new Class[]{String.class, int.class},
                new Object[]{packageName, userHandle});
        if (result == null) {
            throw new RuntimeException("Package manager has died");
        }
        return result;
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags)
            throws NameNotFoundException {
        return mPM.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags)
            throws NameNotFoundException {
        return mPM.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name,
                                                      int flags) throws NameNotFoundException {
        return mPM.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return mPM.getAllPermissionGroups(flags);
    }

    //不可更改， 被 getText、getDrawable、getResourcesForApplicationAsUser 调用
    @Override public Resources getResourcesForApplication(
            ApplicationInfo app) throws NameNotFoundException {
        return mPM.getResourcesForApplication(app);
    }

    //不可更改，被 getText、getDrawable 调用
    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags)
            throws NameNotFoundException {
        return mPM.getApplicationInfo(packageName, flags);
    }

    @Override public Drawable getDrawable(String packageName, int resid,
                                          ApplicationInfo appInfo) {
        return mPM.getDrawable(packageName, resid, appInfo);
    }

    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId)
            throws NameNotFoundException {

        return (Resources)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getResourcesForApplicationAsUser",
                new Class[]{String.class, int.class},
                new Object[]{appPackageName, userId});
    }

    @Override
    public CharSequence getText(String packageName, int resid,
                                ApplicationInfo appInfo) {
        return mPM.getText(packageName, resid, appInfo);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags)
            throws NameNotFoundException {
        return mPM.getActivityInfo(className, flags);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags)
            throws NameNotFoundException {
        return mPM.getReceiverInfo(className, flags);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags)
            throws NameNotFoundException {
        return mPM.getServiceInfo(className, flags);
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName className, int flags)
            throws NameNotFoundException {
        return mPM.getProviderInfo(className, flags);
    }

    @Override
    public String[] getSystemSharedLibraryNames() {
        return mPM.getSystemSharedLibraryNames();
    }

    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return mPM.getSystemAvailableFeatures();
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return mPM.hasSystemFeature(name);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return mPM.checkPermission(permName, pkgName);
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return mPM.addPermission(info);
    }

    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return mPM.addPermissionAsync(info);
    }

    @Override
    public void removePermission(String name) {
        mPM.removePermission(name);
    }

    public void grantPermission(String packageName, String permissionName) {
        RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "grantPermission",
                new Class[]{String.class, String.class},
                new Object[]{packageName, permissionName});
    }

    public void revokePermission(String packageName, String permissionName) {
        RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "revokePermission",
                new Class[]{String.class, String.class},
                new Object[]{packageName, permissionName});
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return mPM.checkSignatures(pkg1, pkg2);
    }

    @Override
    public int checkSignatures(int uid1, int uid2) {
        Integer result = (Integer)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "checkUidSignatures",
                new Class[]{int.class, int.class},
                new Object[]{uid1, uid2});

        if (result == null) {
            throw new RuntimeException("Package manager has died");
        }
        return result;
    }

    @Override
    public String[] getPackagesForUid(int uid) {
        return mPM.getPackagesForUid(uid);
    }

    @Override
    public String getNameForUid(int uid) {
        return mPM.getNameForUid(uid);
    }

    public int getUidForSharedUser(String sharedUserName)
            throws NameNotFoundException {
        Integer result = (Integer)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getUidForSharedUser",
                new Class[]{String.class},
                new Object[]{sharedUserName});

        if (result == null) {
            throw new RuntimeException("Package manager has died");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return getInstalledPackages(flags, mUseId);
    }

    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        List<PackageInfo> list = (List<PackageInfo>)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getInstalledPackages",
                new Class[]{int.class, int.class},
                new Object[]{flags, userId});
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PackageInfo> getPackagesHoldingPermissions(
            String[] permissions, int flags) {
        return mPM.getPackagesHoldingPermissions(permissions, flags);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return mPM.getInstalledApplications(flags);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {

        return resolveActivityAsUser(intent, flags, mUseId);
    }

    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {

        return (ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "resolveActivityAsUser",
                new Class[]{Intent.class, int.class, int.class},
                new Object[]{intent, flags, userId});

    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent,
                                                   int flags) {
        return queryIntentActivitiesAsUser(intent, flags, mUseId);
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent,
                                                         int flags, int userId) {
            return (List<ResolveInfo>)(ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "queryIntentActivitiesAsUser",
                    new Class[]{Intent.class, int.class, int.class},
                    new Object[]{intent, flags, userId});
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(
            ComponentName caller, Intent[] specifics, Intent intent,
            int flags) {
        return mPM.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, int userId) {
            return (List<ResolveInfo>)(ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "queryBroadcastReceivers",
                    new Class[]{Intent.class, int.class, int.class},
                    new Object[]{intent, flags, userId});
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return queryBroadcastReceivers(intent, flags, mUseId);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
            return (ResolveInfo)(ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "resolveService",
                    new Class[]{Intent.class, int.class, int.class},
                    new Object[]{intent, flags});
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
            return (List<ResolveInfo> )(ResolveInfo)(ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "queryIntentServicesAsUser",
                    new Class[]{Intent.class, int.class, int.class},
                    new Object[]{intent, flags, userId});
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return queryIntentServicesAsUser(intent, flags, mUseId);
    }

    public List<ResolveInfo> queryIntentContentProvidersAsUser(
            Intent intent, int flags, int userId) {
        return (List<ResolveInfo> )(ResolveInfo)(ResolveInfo)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "queryIntentContentProvidersAsUser",
                new Class[]{Intent.class, int.class, int.class},
                new Object[]{intent, flags, userId});
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return queryIntentContentProvidersAsUser(intent, flags, mUseId);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name,
                                               int flags) {
        return mPM.resolveContentProvider(name, flags);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName,
                                                    int uid, int flags) {
            return mPM.queryContentProviders(processName, uid, flags);
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(
            ComponentName className, int flags)
            throws NameNotFoundException {
        return mPM.getInstrumentationInfo(className, flags);
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(
            String targetPackage, int flags) {
        return mPM.queryInstrumentation(targetPackage, flags);
    }

    @Override public Drawable getActivityIcon(ComponentName activityName)
            throws NameNotFoundException {
        return getActivityInfo(activityName, 0).loadIcon(this);
    }

    @Override public Drawable getActivityIcon(Intent intent)
            throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityIcon(intent.getComponent());
        }

        ResolveInfo info = resolveActivity(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info != null) {
            return info.activityInfo.loadIcon(this);
        }

        throw new NameNotFoundException(intent.toUri(0));
    }

    @Override public Drawable getDefaultActivityIcon() {
        return mPM.getDefaultActivityIcon();
    }

    @Override public Drawable getApplicationIcon(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    @Override public Drawable getApplicationIcon(String packageName)
            throws NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 0));
    }

    @Override
    public Drawable getActivityLogo(ComponentName activityName)
            throws NameNotFoundException {
        return getActivityInfo(activityName, 0).loadLogo(this);
    }

    @Override
    public Drawable getActivityLogo(Intent intent)
            throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityLogo(intent.getComponent());
        }

        ResolveInfo info = resolveActivity(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info != null) {
            return info.activityInfo.loadLogo(this);
        }

        throw new NameNotFoundException(intent.toUri(0));
    }

    @Override
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return info.loadLogo(this);
    }

    @Override
    public Drawable getApplicationLogo(String packageName)
            throws NameNotFoundException {
        return getApplicationLogo(getApplicationInfo(packageName, 0));
    }

    @Override public Resources getResourcesForActivity(
            ComponentName activityName) throws NameNotFoundException {
        return getResourcesForApplication(
                getActivityInfo(activityName, 0).applicationInfo);
    }

    @Override public Resources getResourcesForApplication(
            String appPackageName) throws NameNotFoundException {
        return getResourcesForApplication(
                getApplicationInfo(appPackageName, 0));
    }

    @Override public boolean isSafeMode() {
       return mPM.isSafeMode();
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resid,
                                    ApplicationInfo appInfo) {
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                return null;
            }
        }
        try {
            Resources r = getResourcesForApplication(appInfo);
            return r.getXml(resid);
        } catch (RuntimeException e) {
            // If an exception was thrown, fall through to return
            // default icon.
            Log.w("PackageManager", "Failure retrieving xml 0x"
                    + Integer.toHexString(resid) + " in package "
                    + packageName, e);
        } catch (NameNotFoundException e) {
            Log.w("PackageManager", "Failure retrieving resources for "
                    + appInfo.packageName);
        }
        return null;
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return info.loadLabel(this);
    }

    public void installPackage(Uri packageURI, Object observer, int flags,
                               String installerPackageName) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "installPackage",
                    new Class[]{Uri.class,
                            Class.forName("android.content.pm.IPackageInstallObserver"),
                    int.class, String.class},
                    new Object[]{observer, flags, installerPackageName});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void installPackageWithVerification(Uri packageURI, Object observer,
                                               int flags, String installerPackageName, Uri verificationURI,
                                               Object manifestDigest, Object encryptionParams) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "installPackageWithVerification",
                    new Class[]{Uri.class,
                            Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class, Uri.class,
                            Class.forName("android.content.pm.ManifestDigest"),
                            Class.forName("android.content.pm.ContainerEncryptionParams")},
                    new Object[]{packageURI, observer, flags, installerPackageName, verificationURI, manifestDigest, encryptionParams});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void installPackageWithVerificationAndEncryption(Uri packageURI,
                                                            Object observer, int flags, String installerPackageName,
                                                            Object verificationParams, Object encryptionParams) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "installPackageWithVerificationAndEncryption",
                    new Class[]{Uri.class,
                            Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class,
                            Class.forName("android.content.pm.VerificationParams"),
                            Class.forName("android.content.pm.ContainerEncryptionParams")},
                    new Object[]{packageURI, observer, flags, installerPackageName, verificationParams, encryptionParams});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public int installExistingPackage(String packageName)
            throws NameNotFoundException {
        Integer integer = (Integer)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "installExistingPackage",
                new Class[]{String.class}, new Object[]{packageName});
        if (integer != null) {
            return integer;
        }
        return -1;
    }

    @Override
    public void verifyPendingInstall(int id, int response) {
            mPM.verifyPendingInstall(id, response);
    }

    @Override
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout,
                                          long millisecondsToDelay) {
            mPM.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    @Override
    public void setInstallerPackageName(String targetPackage,
                                        String installerPackageName) {
            mPM.setInstallerPackageName(targetPackage, installerPackageName);
    }

    public void movePackage(String packageName, Object observer, int flags) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "movePackage",
                    new Class[]{String.class, Class.forName("android.content.pm.IPackageMoveObserver"), int.class}, new Object[]{packageName, observer, flags});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getInstallerPackageName(String packageName) {
        return mPM.getInstallerPackageName(packageName);
    }

    public void deletePackage(String packageName, Object observer, int flags) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "deletePackage",
                    new Class[]{String.class, Class.forName("android.content.pm.IPackageDeleteObserver"), int.class},
                    new Object[]{packageName, observer, flags});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void clearApplicationUserData(String packageName,
                                         Object observer) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "clearApplicationUserData",
                    new Class[]{String.class, Class.forName("android.content.pm.IPackageDataObserver")},
                    new Object[]{packageName, observer});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteApplicationCacheFiles(String packageName,
                                            Object observer) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "deleteApplicationCacheFiles",
                    new Class[]{String.class, Class.forName("android.content.pm.IPackageDataObserver")},
                    new Object[]{packageName, observer});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void freeStorageAndNotify(long idealStorageSize, Object observer) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "freeStorageAndNotify",
                    new Class[]{long.class, Class.forName("android.content.pm.IPackageDataObserver")},
                    new Object[]{idealStorageSize, observer});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void freeStorage(long freeStorageSize, IntentSender pi) {
        RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "freeStorage",
                    new Class[]{long.class, IntentSender.class},
                    new Object[]{freeStorageSize, pi});
    }

    public void getPackageSizeInfo(String packageName, int userHandle,
                                   Object observer) {
        try {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getPackageSizeInfo",
                    new Class[]{String.class, int.class, Class.forName("android.content.pm.IPackageStatsObserver")},
                    new Object[]{packageName, userHandle, observer});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void addPackageToPreferred(String packageName) {
            mPM.addPackageToPreferred(packageName);
    }

    @Override
    public void removePackageFromPreferred(String packageName) {
            mPM.removePackageFromPreferred(packageName);
    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
            return mPM.getPreferredPackages(flags);
    }

    @Override
    public void addPreferredActivity(IntentFilter filter,
                                     int match, ComponentName[] set, ComponentName activity) {
            RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "addPreferredActivity",
                    new Class[]{IntentFilter.class, int.class, ComponentName[].class, ComponentName.class},
                    new Object[]{filter, match, set, activity});
    }

    public void addPreferredActivity(IntentFilter filter, int match,
                                     ComponentName[] set, ComponentName activity, int userId) {

        RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "addPreferredActivity",
                new Class[]{IntentFilter.class, int.class, ComponentName[].class, ComponentName.class, int.class},
                new Object[]{filter, match, set, activity, userId});

    }

    public void replacePreferredActivity(IntentFilter filter,
                                         int match, ComponentName[] set, ComponentName activity) {
        RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "replacePreferredActivity",
                new Class[]{IntentFilter.class, int.class, ComponentName[].class, ComponentName.class},
                new Object[]{filter, match, set, activity});

    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {
            mPM.clearPackagePreferredActivities(packageName);
    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters,
                                      List<ComponentName> outActivities, String packageName) {
            return mPM.getPreferredActivities(outFilters, outActivities, packageName);
    }

    public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {

        ComponentName cn = (ComponentName)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getHomeActivities",
                new Class[]{List.class},
                new Object[]{outActivities});

        return cn;
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName,
                                           int newState, int flags) {
            mPM.setComponentEnabledSetting(componentName, newState, flags);
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
            return mPM.getComponentEnabledSetting(componentName);
    }

    @Override
    public void setApplicationEnabledSetting(String packageName,
                                             int newState, int flags) {
            mPM.setApplicationEnabledSetting(packageName, newState, flags);
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
            return mPM.getApplicationEnabledSetting(packageName);
    }

    public boolean setApplicationBlockedSettingAsUser(String packageName, boolean blocked,
                                                      UserHandle user) {
        Boolean result = (Boolean)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "setApplicationBlockedSettingAsUser",
                new Class[]{String.class, boolean.class, UserHandle.class},
                new Object[]{packageName, blocked, user});

        if (result != null) {
            return result;
        }
        return false;
    }

    public boolean getApplicationBlockedSettingAsUser(String packageName, UserHandle user) {
        Boolean result = (Boolean)RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getApplicationBlockedSettingAsUser",
                new Class[]{String.class, UserHandle.class},
                new Object[]{packageName, user});

        if (result != null) {
            return result;
        }
        return false;
    }

    public Object getVerifierDeviceIdentity() {
        return RefInvoker.invokeMethod(mPM, PackageManager.class.getName(), "getVerifierDeviceIdentity",
                (Class[]) null,
                (Object[]) null);
    }

}
