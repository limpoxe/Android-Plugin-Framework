package com.plugin.core.app;

import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginAppTrace;
import com.plugin.core.PluginInstrumentionWrapper;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ActivityThread {

    private static final String android_app_ActivityThread = "android.app.ActivityThread";
    private static final String android_app_ActivityThread_currentActivityThread = "currentActivityThread";
    private static final String android_app_ActivityThread_mInstrumentation = "mInstrumentation";
    private static final String android_app_ActivityThread_getHandler = "getHandler";
    private static final String android_app_ActivityThread_installContentProviders = "installContentProviders";
    private static final String android_app_ActivityThread_AppBindData = "android.app.ActivityThread$AppBindData";
    private static final String android_app_ActivityThread_mServices = "mServices";

    private static final String android_os_Handler_mCallback = "mCallback";

    private static final String android_app_ContextImpl = "android.app.ContextImpl";
    private static final String android_app_ContextImpl_getImpl = "getImpl";
    private static final String android_app_ContextImpl_mMainThread = "mMainThread";

    private static Object sCurrentActivityThread;
    private static Class sClass;

    public static Class clazz() {
        if (sClass == null) {
            try {
                sClass = Class.forName(android_app_ActivityThread);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sClass;
    }

    public synchronized static Object currentActivityThread() {
        if (sCurrentActivityThread == null) {
            // 从ThreadLocal中取出来的
            LogUtil.d("从宿主程序中取出ActivityThread对象备用");
            sCurrentActivityThread = RefInvoker.invokeStaticMethod(android_app_ActivityThread,
                    android_app_ActivityThread_currentActivityThread,
                    (Class[]) null, (Object[]) null);

            //有些情况下上面的方法拿不到，下面再换个方法尝试一次
            if (sCurrentActivityThread == null) {
                Object impl = RefInvoker.invokeStaticMethod(android_app_ContextImpl, android_app_ContextImpl_getImpl,
                        new Class[]{Context.class}, new Object[]{PluginLoader.getApplication()});
                if (impl != null) {
                    sCurrentActivityThread = RefInvoker.getFieldObject(impl, android_app_ContextImpl, android_app_ContextImpl_mMainThread);
                }
            }
        }
        return sCurrentActivityThread;
    }

    public static Object getResCompatibilityInfo() {
        //貌似没啥用
        Object mBoundApplication = RefInvoker.getFieldObject(currentActivityThread(), android_app_ActivityThread, "mBoundApplication");
        Object compatInfo = RefInvoker.getFieldObject(mBoundApplication, android_app_ActivityThread_AppBindData, "compatInfo");
        return compatInfo;
    }

    public static void installContentProviders(Context context, List<ProviderInfo> providers) {
        RefInvoker.invokeMethod(currentActivityThread(),
                clazz(), android_app_ActivityThread_installContentProviders,
                new Class[]{Context.class, List.class}, new Object[]{context, providers});
    }

    public static void wrapHandler() {
        Handler handler = (Handler)RefInvoker.invokeMethod(currentActivityThread(),
                clazz(), android_app_ActivityThread_getHandler,
                (Class[]) null, (Object[]) null);
        RefInvoker.setFieldObject(handler, Handler.class.getName(), android_os_Handler_mCallback,
                new PluginAppTrace(handler));
    }

    public static void wrapInstrumentation() {
        Instrumentation originalInstrumentation = (Instrumentation) RefInvoker.getFieldObject(currentActivityThread(),
                clazz(), android_app_ActivityThread_mInstrumentation);;
        RefInvoker.setFieldObject(currentActivityThread(), clazz(),
                android_app_ActivityThread_mInstrumentation,
                new PluginInstrumentionWrapper(originalInstrumentation));
    }

    //getPackageInfo(ApplicationInfo aInfo, CompatibilityInfo compatInfo,
    //               ClassLoader baseLoader, boolean securityViolation, boolean includeCode)


//    synchronized (mResourcesManager) {
//        1834            WeakReference<LoadedApk> ref;
//        1835            if (includeCode) {
//            1836                ref = mPackages.get(aInfo.packageName);
//            1837            } else {
//            1838                ref = mResourcePackages.get(aInfo.packageName);
//            1839            }
//        1840            LoadedApk packageInfo = ref != null ? ref.get() : null;
//        1841            if (packageInfo == null || (packageInfo.mResources != null
//        1842                    && !packageInfo.mResources.getAssets().isUpToDate())) {
//            1843                if (localLOGV) Slog.v(TAG, (includeCode ? "Loading code package "
//                    1844                        : "Loading resource-only package ") + aInfo.packageName
//            1845                        + " (in " + (mBoundApplication != null
//            1846                                ? mBoundApplication.processName : null)
//            1847                        + ")");
//            1848                packageInfo =
//                    1849                    new LoadedApk(this, aInfo, compatInfo, this, baseLoader,
//                    1850                            securityViolation, includeCode &&
//                    1851                            (aInfo.flags&ApplicationInfo.FLAG_HAS_CODE) != 0);
//            1852                if (includeCode) {
//                1853                    mPackages.put(aInfo.packageName,
//                        1854                            new WeakReference<LoadedApk>(packageInfo));
//                1855                } else {
//                1856                    mResourcePackages.put(aInfo.packageName,
//                        1857                            new WeakReference<LoadedApk>(packageInfo));
//                1858                }
//            1859            }
//        1860            return packageInfo;
//        1861        }


//LoadedApk 构造器
//    mActivityThread = activityThread;
//    117        mApplicationInfo = aInfo;
//    118        mPackageName = aInfo.packageName;
//    119        mAppDir = aInfo.sourceDir;
//    120        final int myUid = Process.myUid();
//    121        mResDir = aInfo.uid == myUid ? aInfo.sourceDir
//    122                : aInfo.publicSourceDir;
//    123        if (!UserHandle.isSameUser(aInfo.uid, myUid) && !Process.isIsolated()) {
//        124            aInfo.dataDir = PackageManager.getDataDirForUser(UserHandle.getUserId(myUid),
//                125                    mPackageName);
//        126        }
//    127        mSharedLibraries = aInfo.sharedLibraryFiles;
//    128        mDataDir = aInfo.dataDir;
//    129        mDataDirFile = mDataDir != null ? new File(mDataDir) : null;
//    130        mLibDir = aInfo.nativeLibraryDir;
//    131        mBaseClassLoader = baseLoader;
//    132        mSecurityViolation = securityViolation;
//    133        mIncludeCode = includeCode;
//    134        mDisplayAdjustments.setCompatibilityInfo(compatInfo);
//    135
//            136        if (mAppDir == null) {
//        137            if (ActivityThread.mSystemContext == null) {
//            138                ActivityThread.mSystemContext =
//                    139                    ContextImpl.createSystemContext(mainThread);
//            140                ResourcesManager resourcesManager = ResourcesManager.getInstance();
//            141                ActivityThread.mSystemContext.getResources().updateConfiguration(
//                    142                        resourcesManager.getConfiguration(),
//                    143                        resourcesManager.getDisplayMetricsLocked(
//                            144                                 Display.DEFAULT_DISPLAY, mDisplayAdjustments), compatInfo);
//            145                //Slog.i(TAG, "Created system resources "
//            146                //        + mSystemContext.getResources() + ": "
//            147                //        + mSystemContext.getResources().getConfiguration());
//            148            }
//        149            mClassLoader = ActivityThread.mSystemContext.getClassLoader();
//        150            mResources = ActivityThread.mSystemContext.getResources();
//        151        }
//
//    ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
//    39
//            40        synchronized (mLoaders) {
//        41            if (parent == null) {
//            42                parent = baseParent;
//            43            }
//
    public static void installPackageInfo(Context hostContext, String pluginId, PluginDescriptor pluginDescriptor,
                                          ClassLoader pluginClassLoader, Resources pluginResource,
                                          Application pluginApplication) throws ClassNotFoundException {

        Object applicationLoaders = RefInvoker.invokeStaticMethod("android.app.ApplicationLoaders", "getDefault", (Class[]) null, (Object[]) null);
        Map mLoaders = (Map)RefInvoker.getFieldObject(applicationLoaders, "android.app.ApplicationLoaders", "mLoaders");

        mLoaders.put(pluginDescriptor.getInstalledPath(), pluginClassLoader);
        try {
            ApplicationInfo info = hostContext.getPackageManager().getApplicationInfo(pluginId, PackageManager.GET_SHARED_LIBRARY_FILES);
            Object compatibilityInfo = getResCompatibilityInfo();//Not Sure
            //先保存
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //会触发替换
            Object pluginLoadedApk = RefInvoker.invokeMethod(currentActivityThread(), android_app_ActivityThread, "getPackageInfoNoCheck",
                    new Class[]{ApplicationInfo.class, Class.forName("android.content.res.CompatibilityInfo")},
                    new Object[]{info, compatibilityInfo});
            if (pluginLoadedApk != null) {
                Class loadedAPKClass = pluginLoadedApk.getClass();
                RefInvoker.setFieldObject(pluginLoadedApk, loadedAPKClass, "mApplication", pluginApplication);
                RefInvoker.setFieldObject(pluginLoadedApk, loadedAPKClass, "mResources", pluginResource);
                RefInvoker.setFieldObject(pluginLoadedApk, loadedAPKClass, "mDataDirFile", new File(PluginLoader.getApplication().getApplicationInfo().dataDir));
                RefInvoker.setFieldObject(pluginLoadedApk, loadedAPKClass, "mDataDir", PluginLoader.getApplication().getApplicationInfo().dataDir);
                //TODO 需要时再说
                //RefInvoker.setFieldObject(pluginLoadedApk, loadedAPKClass, "mLibDir", );
            }
            //再还原
            Thread.currentThread().setContextClassLoader(classLoader);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Map<IBinder, Service> getAllServices() {
        Map<IBinder, Service> services = (Map<IBinder, Service>)RefInvoker.getFieldObject(currentActivityThread(), android_app_ActivityThread, android_app_ActivityThread_mServices);
        return services;
    }

}
