package com.limpoxe.fairy.core.android;

import android.app.Application;
import android.content.res.Resources;

import com.limpoxe.fairy.util.RefInvoker;

import java.io.File;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackLoadedApk {
    private static final String ClassName = "android.app.LoadedApk";

    private static final String Field_mApplication = "mApplication";
    private static final String Field_mResources = "mResources";
    private static final String Field_mDataDirFile = "mDataDirFile";
    private static final String Field_mDataDir = "mDataDir";
    private static final String Field_mLibDir = "mLibDir";
    private static final String Field_mClassLoader = "mClassLoader";
    private static final String Field_mActivityThread = "mActivityThread";

    private Object instance;

    public HackLoadedApk(Object instance) {
        this.instance = instance;
    }

    public void setApplication(Application pluginApplication) {
        RefInvoker.setField(instance, ClassName, Field_mApplication, pluginApplication);
    }

    public void setResources(Resources pluginResource) {
        RefInvoker.setField(instance, ClassName, Field_mResources, pluginResource);
    }

    public void setDataDirFile(File dirFile) {
        RefInvoker.setField(instance, ClassName, Field_mDataDirFile, dirFile);
    }

    public void setDataDir(String dataDir) {
        RefInvoker.setField(instance, ClassName, Field_mDataDir, dataDir);
    }

    public void setLibDir(String libDir) {
        RefInvoker.setField(instance, ClassName, Field_mLibDir, libDir);
    }

    public ClassLoader getClassLoader() {
        return (ClassLoader) RefInvoker.getField(instance, ClassName, Field_mClassLoader);
    }

    public void setClassLoader(ClassLoader classLoader) {
        RefInvoker.setField(instance, ClassName, Field_mClassLoader, classLoader);
    }

    public Object getActivityThread() {
        return RefInvoker.getField(instance, ClassName, Field_mActivityThread);
    }
}
