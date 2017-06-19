package com.limpoxe.fairy.core.android;

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

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginAppTrace;
import com.limpoxe.fairy.core.PluginInstrumentionWrapper;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.File;
import java.util.List;
import java.util.Map;

public class HackActivityThread {

    private static final String ClassName = "android.app.ActivityThread";

    private static final String Method_currentActivityThread = "currentActivityThread";
    private static final String Method_getHandler = "getHandler";
    private static final String Method_installContentProviders = "installContentProviders";
    private static final String Method_getPackageInfoNoCheck = "getPackageInfoNoCheck";

    private static final String Field_mInstrumentation = "mInstrumentation";
    private static final String Field_mServices = "mServices";
    private static final String Field_mBoundApplication = "mBoundApplication";
    private static final String Field_sPackageManager = "sPackageManager";

    private static final String Field_SERVICE_DONE_EXECUTING_ANON = "SERVICE_DONE_EXECUTING_ANON";
    private static final String Field_SERVICE_DONE_EXECUTING_START = "SERVICE_DONE_EXECUTING_START";
    private static final String Field_SERVICE_DONE_EXECUTING_STOP = "SERVICE_DONE_EXECUTING_STOP";

    private static HackActivityThread hackActivityThread;

    private Object instance;

    private HackActivityThread(Object instance) {
        this.instance = instance;
    }

    public static synchronized HackActivityThread get() {
        if (hackActivityThread == null) {
            Object instance = currentActivityThread();
            if (instance != null) {
                hackActivityThread = new HackActivityThread(instance);
            }
        }
        return hackActivityThread;
    }

    public static Class clazz() {
        try {
            return RefInvoker.forName(ClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object currentActivityThread() {
        // 从ThreadLocal中取出来的
        LogUtil.v("从宿主程序中取出ActivityThread对象备用");
        Object sCurrentActivityThread = RefInvoker.invokeMethod(null, ClassName,
                Method_currentActivityThread,
                (Class[]) null, (Object[]) null);

        //有些情况下上面的方法拿不到，下面再换个方法尝试一次
        if (sCurrentActivityThread == null) {
            Object impl = HackContextImpl.getImpl(FairyGlobal.getApplication());
            if (impl != null) {
                sCurrentActivityThread = new HackContextImpl(impl).getMainThread();
            }
        }
        return sCurrentActivityThread;
    }

    public static void wrapHandler() {
        HackActivityThread hackActivityThread = get();
        if (hackActivityThread != null) {
            Handler handler = hackActivityThread.getHandler();
            Handler.Callback callback = new PluginAppTrace(handler);
            new HackHandler(handler).setCallback(callback);
        } else {
            LogUtil.e("wrapHandler fail!!");
        }
    }

    public static void wrapInstrumentation() {
        HackActivityThread hackActivityThread = get();
        if (hackActivityThread != null) {
            Instrumentation originalInstrumentation = hackActivityThread.getInstrumentation();
            if (!(originalInstrumentation instanceof PluginInstrumentionWrapper)) {
                hackActivityThread.setInstrumentation(new PluginInstrumentionWrapper(originalInstrumentation));
            }
        } else {
            LogUtil.e("wrapInstrumentation fail!!");
        }
    }

    public static Object getResCompatibilityInfo() {
        //貌似没啥用
        HackActivityThread hackActivityThread = get();
        if (hackActivityThread != null) {
            Object mBoundApplication = hackActivityThread.getBoundApplicationData();
            Object compatInfo = new HackAppBindData(mBoundApplication).getCompatInfo();
            return compatInfo;
        }
        return null;
    }

    public static Object getLoadedApk() {
        HackActivityThread hackActivityThread = get();
        if (hackActivityThread != null) {
            //貌似没啥用
            Object mBoundApplication = hackActivityThread.getBoundApplicationData();
            Object info = new HackAppBindData(mBoundApplication).getInfo();
            return info;
        }
        return null;
    }

    //For TabHostActivity
    public static void installPackageInfo(Context hostContext, String pluginId, PluginDescriptor pluginDescriptor,
                                          ClassLoader pluginClassLoader, Resources pluginResource,
                                          Application pluginApplication) throws ClassNotFoundException {

        Object applicationLoaders = RefInvoker.invokeMethod(null, "android.app.ApplicationLoaders", "getDefault", (Class[]) null, (Object[]) null);
        Map mLoaders = (Map)RefInvoker.getField(applicationLoaders, "android.app.ApplicationLoaders", "mLoaders");
        if (mLoaders == null) {
            //what!!
            return;
        }
        mLoaders.put(pluginDescriptor.getInstalledPath(), pluginClassLoader);
        try {
            //先保存
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //会触发替换
            HackActivityThread hackActivityThread = HackActivityThread.get();
            if (hackActivityThread != null) {
                ApplicationInfo info = hostContext.getPackageManager().getApplicationInfo(pluginId, PackageManager.GET_SHARED_LIBRARY_FILES);
                Object compatibilityInfo = getResCompatibilityInfo();//Not Sure
                Object pluginLoadedApk = hackActivityThread.getPackageInfoNoCheck(info, compatibilityInfo);
                if (pluginLoadedApk != null) {
                    HackLoadedApk loadedApk = new HackLoadedApk(pluginLoadedApk);
                    loadedApk.setApplication(pluginApplication);
                    loadedApk.setResources(pluginResource);
                    loadedApk.setDataDirFile(new File(FairyGlobal.getApplication().getApplicationInfo().dataDir));
                    loadedApk.setDataDir(FairyGlobal.getApplication().getApplicationInfo().dataDir);
                    //TODO 需要时再说
                    //loadedApk.setLibDir();
                }
            }
            //再还原
            Thread.currentThread().setContextClassLoader(classLoader);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object getBoundApplicationData() {
        Object mBoundApplication = RefInvoker.getField(instance, ClassName, Field_mBoundApplication);
        return mBoundApplication;
    }

    public void installContentProviders(Context context, List<ProviderInfo> providers) {
        RefInvoker.invokeMethod(instance,
                ClassName, Method_installContentProviders,
                new Class[]{Context.class, List.class}, new Object[]{context, providers});
    }

    public Handler getHandler() {
        return (Handler)RefInvoker.invokeMethod(instance,
                ClassName, Method_getHandler,
                (Class[]) null, (Object[]) null);
    }

    public Instrumentation getInstrumentation() {
        return (Instrumentation) RefInvoker.getField(instance,
                ClassName, Field_mInstrumentation);
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        RefInvoker.setField(instance, ClassName,
                Field_mInstrumentation,
                instrumentation);
    }

    public Map<IBinder, Service> getServices() {
        Map<IBinder, Service> services = (Map<IBinder, Service>)RefInvoker.getField(instance, ClassName, Field_mServices);
        return services;
    }

    public Object getPackageInfoNoCheck(ApplicationInfo info, Object compatibilityInfo) {
        try {
            Object pluginLoadedApk = RefInvoker.invokeMethod(instance, ClassName, Method_getPackageInfoNoCheck,
                    new Class[]{ApplicationInfo.class, Class.forName("android.content.res.CompatibilityInfo")},
                    new Object[]{info, compatibilityInfo});
            return pluginLoadedApk;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getPackageManager() {
        return RefInvoker.getField(null, ClassName, Field_sPackageManager);
    }

    public static void setPackageManager(Object packageManager) {
        RefInvoker.setField(null, ClassName, Field_sPackageManager, packageManager);
    }

    public static Integer getSERVICE_DONE_EXECUTING_ANON() {
        Integer ret = (Integer) RefInvoker.getField(null, ClassName, Field_SERVICE_DONE_EXECUTING_ANON);
        if (ret == null) {
            ret = 0;//default is 0
        }
        return ret;
    }

}
