package com.limpoxe.fairy.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
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
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.*;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackActivity;

import java.lang.reflect.Field;
import java.util.List;

public class FakeUtil {

    /**
     * 由于插件的getPackageName返回的是插件包名
     * 实际应用中一些第三方库可能需要使用宿主包名, 此时可以通过此方法
     * 对插件的Context的包名进行修正
     * @param context
     * @return
     */
    public static Context fakeContext(Context context) {
        if (!context.getPackageName().equals(FairyGlobal.getHostApplication().getPackageName())) {
            context = new ContextWrapper(context) {
                @Override
                public String getPackageName() {
                    return FairyGlobal.getHostApplication().getPackageName();
                }
            };
        }
        return context;
    }

    /**
     * 需要fakeContext并不是因为插件取不到自己的meta-data，
     * 而是因为针对需要appkey的sdk插件，需要同时正确的取到下面3个值，
     *   1、packageName
     *   2、meta-data
     *   3、signatures
     *
     * 默认情况是：
     *   1、当packageName是宿主的时候，meta-data和signatures都会取到宿主的值
     *   2、当packageName是插件的时候，meta-data和signatures都会取到插件的值
     *
     * 然而针对需要appkey的sdk插件来说，一般情况是这样：
     *   1、packageName需要是宿主，因为appkey是注册的宿主的appkey
     *   2、meta-data需要是插件，因为meta-data是放在插件的manifeat文件中
     *   3、signatures需要是宿主，因为appkey是注册的宿主的signatures
     * 因此需要fackeContext来实现这种组合
     *
     * 但是，如果注册appkey的时候，就直接使用了插件的packageName、meta-data、signatures
     * 则无需使用fakeContext，直接使用插件的Application即可。
     *
     */
    public static Context fakeApplication(final Application application) {
        Application fakeForSdk = new Application() {

            @Override
            public String getPackageName() {
                Context context = getBaseContext();
                while (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                }
                //返回宿主packageName
                return context.getPackageName();
            }

            @Override
            public PackageManager getPackageManager() {
                final PackageManager pm = super.getPackageManager();

                return new PackageManager() {

                    @Override
                    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
                        PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
                        if ((flags & PackageManager.GET_SIGNATURES) != 0 ) {
                            //返回宿主签名
                            packageInfo.signatures = pm.getPackageInfo(getPackageName(), flags).signatures;
                        }
                        return packageInfo;
                    }

                    //Android-O
                    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int i) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
                        ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, flags);
                        if ((flags & PackageManager.GET_META_DATA) != 0 ) {
                            Context context = getBaseContext();
                            while (context instanceof ContextWrapper) {
                                context = ((ContextWrapper) context).getBaseContext();
                            }
                            //返回宿主meta
                            applicationInfo.metaData = context.getApplicationInfo().metaData;
                        }
                        return applicationInfo;
                    }

                    @Override
                    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
                        return pm.getActivityInfo(component, flags);
                    }

                    @Override
                    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {
                        return pm.getReceiverInfo(component, flags);
                    }

                    @Override
                    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
                        return pm.getServiceInfo(component, flags);
                    }

                    @Override
                    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
                        return pm.getProviderInfo(component, flags);
                    }

                    @Override
                    public List<PackageInfo> getInstalledPackages(int flags) {
                        return pm.getInstalledPackages(flags);
                    }

                    @Override
                    public int checkSignatures(String pkg1, String pkg2) {
                        return pm.checkSignatures(pkg1, pkg2);
                    }

                    @Override
                    public int checkSignatures(int uid1, int uid2) {
                        return pm.checkSignatures(uid1, uid2);
                    }

                    @Override
                    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
                        return pm.queryBroadcastReceivers(intent, flags);
                    }

                    //methods belows is not need, remain empty impl
                    @Override
                    public String[] currentToCanonicalPackageNames(String[] names) {
                        return new String[0];
                    }

                    @Override
                    public String[] canonicalToCurrentPackageNames(String[] names) {
                        return new String[0];
                    }

                    @Override
                    public Intent getLaunchIntentForPackage(String packageName) {
                        return null;
                    }

                    @Override
                    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
                        return null;
                    }

                    @Override
                    public int[] getPackageGids(String packageName) throws NameNotFoundException {
                        return new int[0];
                    }

                    //@Override //android-N
                    public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
                        return new int[0];
                    }

                    @Override
                    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
                        return null;
                    }

                    @Override
                    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
                        return null;
                    }

                    @Override
                    public int checkPermission(String permName, String pkgName) {
                        return PackageManager.PERMISSION_GRANTED;
                    }

                    @Override
                    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
                        return false;
                    }

                    @Override
                    public boolean addPermission(PermissionInfo info) {
                        return false;
                    }

                    @Override
                    public boolean addPermissionAsync(PermissionInfo info) {
                        return false;
                    }

                    @Override
                    public void removePermission(String name) {

                    }

                    @Override
                    public String[] getPackagesForUid(int uid) {
                        return new String[0];
                    }

                    @Override
                    public String getNameForUid(int uid) {
                        return null;
                    }

                    @Override
                    public List<ApplicationInfo> getInstalledApplications(int flags) {
                        return null;
                    }

                    //Android-O
                    public boolean isInstantApp() {
                        return false;
                    }

                    //Android-O
                    public boolean isInstantApp(String s) {
                        return false;
                    }

                    //Android-O
                    public int getInstantAppCookieMaxBytes() {
                        return 0;
                    }

                    //Android-O
                    public byte[] getInstantAppCookie() {
                        return new byte[0];
                    }

                    //Android-O
                    public void clearInstantAppCookie() {

                    }

                    //Android-O
                    public void updateInstantAppCookie(byte[] bytes) {

                    }

                    @Override
                    public String[] getSystemSharedLibraryNames() {
                        return new String[0];
                    }

                    //Android-O
                    public List<SharedLibraryInfo> getSharedLibraries(int i) {
                        return null;
                    }

                    //Android-O
                    public ChangedPackages getChangedPackages(int i) {
                        return null;
                    }

                    @Override
                    public FeatureInfo[] getSystemAvailableFeatures() {
                        return new FeatureInfo[0];
                    }

                    @Override
                    public boolean hasSystemFeature(String name) {
                        return false;
                    }

                    @Override
                    public ResolveInfo resolveActivity(Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public ResolveInfo resolveService(Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
                        return null;
                    }

                    @Override
                    public ProviderInfo resolveContentProvider(String name, int flags) {
                        return null;
                    }

                    @Override
                    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
                        return null;
                    }

                    @Override
                    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
                        return null;
                    }

                    @Override
                    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
                        return null;
                    }

                    @Override
                    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getDefaultActivityIcon() {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationIcon(ApplicationInfo info) {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationBanner(ApplicationInfo info) {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationLogo(ApplicationInfo info) {
                        return null;
                    }

                    @Override
                    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
                        return null;
                    }

                    @Override
                    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
                        return null;
                    }

                    @Override
                    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
                        return null;
                    }

                    @Override
                    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
                        return null;
                    }

                    @Override
                    public android.content.res.XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
                        return null;
                    }

                    @Override
                    public CharSequence getApplicationLabel(ApplicationInfo info) {
                        return null;
                    }

                    @Override
                    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
                        return null;
                    }

                    @Override
                    public void verifyPendingInstall(int id, int verificationCode) {

                    }

                    @Override
                    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {

                    }

                    @Override
                    public void setInstallerPackageName(String targetPackage, String installerPackageName) {

                    }

                    @Override
                    public String getInstallerPackageName(String packageName) {
                        return null;
                    }

                    @Override
                    public void addPackageToPreferred(String packageName) {

                    }

                    @Override
                    public void removePackageFromPreferred(String packageName) {

                    }

                    @Override
                    public List<PackageInfo> getPreferredPackages(int flags) {
                        return null;
                    }

                    @Override
                    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {

                    }

                    @Override
                    public void clearPackagePreferredActivities(String packageName) {

                    }

                    @Override
                    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
                        return 0;
                    }

                    @Override
                    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {

                    }

                    @Override
                    public int getComponentEnabledSetting(ComponentName componentName) {
                        return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
                    }

                    @Override
                    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {

                    }

                    @Override
                    public int getApplicationEnabledSetting(String packageName) {
                        return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
                    }

                    @Override
                    public boolean isSafeMode() {
                        return false;
                    }

                    //Android-O
                    public void setApplicationCategoryHint(String s, int i) {

                    }

                    @Override
                    public PackageInstaller getPackageInstaller() {
                        return null;
                    }

                    //Android-O
                    public boolean canRequestPackageInstalls() {
                        return false;
                    }

                    //@Override //android-N
                    public boolean hasSystemFeature(String name,int flag) {
                        return false;
                    }

                    //@Override //android-N
                    public int getPackageUid(String name,int flag) {
                        return 0;
                    }
                };
            }
        };
        try {
            Field base = ContextWrapper.class.getDeclaredField("mBase");
            base.setAccessible(true);
            Context c = (Context)base.get(application);
            base.set(fakeForSdk, c);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            LogUtil.printException("FakeUtil.fakeApplication", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LogUtil.printException("FakeUtil.fakeApplication", e);
        }

        return fakeForSdk;
    }

    /**
     * @param pluginContext 参数为插件的context，例如插件activity或者插件Application
     * @return
     */
    public static String getHostPackageName(ContextWrapper pluginContext) {
        Context context = pluginContext;
        while (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        //到这里context的实际类型应当是ContextImpl类，可以返回宿主packageName
        return context.getPackageName();
    }

    public static Context fakeWindowContext(final Activity pluginActivity) {
        return new ContextWrapper(FairyGlobal.getHostApplication()) {
            @Override
            public Object getSystemService(String name) {
                if (WINDOW_SERVICE.equals(name)) {
                    return pluginActivity.getSystemService(name);
                }
                return super.getSystemService(name);
            }
        };
    }

    public static Activity fakeActivityForUMengSdk(Activity activity) {
        //getHostApplication();
        //getApplicationContext();
        //getPackageName();
        //getLocalClassName();
        final String className = activity.getClass().getSimpleName();
        Activity fakeActivity = new Activity() {
            @Override
            public Context getApplicationContext() {
                return FairyGlobal.getHostApplication().getApplicationContext();
            }

            @Override
            public String getPackageName() {
                return FairyGlobal.getHostApplication().getPackageName();
            }

            public String getLocalClassName() {
                return className;
            }
        };
        new HackActivity(fakeActivity).setApplication(FairyGlobal.getHostApplication());
        return fakeActivity;
    }
}
