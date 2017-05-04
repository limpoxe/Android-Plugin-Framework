package com.limpoxe.fairy.core;

import android.app.Application;

import com.limpoxe.fairy.util.LogUtil;

public class FairyGlobal {
    private static boolean sIsInited;
    private static Application sApplication;
    private static boolean sIsLocalHtmlEnable = true;
    private static int sLoadingResId;
    private static long sMinLoadingTime = 400;

    public static Application getApplication() {
        if (!isInited()) {
            throw new IllegalStateException("not inited yet");
        }
        return sApplication;
    }

    /*package*/ static void setApplication(Application application) {
        sApplication = application;
    }

    /*package*/ static void setIsInited(boolean isInited) {
        sIsInited = isInited;
    }

    /*package*/ static boolean isInited() {
        return sIsInited;
    }

    /**
     * 插件中是否支持使用本地html文件
     * @param isLocalHtmlEnable
     */
    public static void setLocalHtmlenable(boolean isLocalHtmlEnable) {
        sIsLocalHtmlEnable = isLocalHtmlEnable;
    }

    public static boolean isLocalHtmlEnable() {
        return sIsLocalHtmlEnable;
    }

    public static void setLogEnable(boolean isLogEnable) {
        LogUtil.setEnable(isLogEnable);
    }

    /**
     * 首次打开插件时，如果是通过Activity打开，会显示一个空白loading页，
     * 通过resId设置loading页ui
     * @param resId
     */
    public static void setLoadingResId(int resId) {
        sLoadingResId = resId;
    }

    public static int getLoadingResId() {
        return sLoadingResId;
    }

    /**
     * 设置loading页最小等待时间，用于在插件较简单，初始化较快时，避免loading页一闪而过
     * 时间设置为0表示无loading页
     * @param minLoadingTime
     */
    public static void setMinLoadingTime(long minLoadingTime) {
        sMinLoadingTime = minLoadingTime;
    }

    public static long getMinLoadingTime() {
        return sMinLoadingTime;
    }

}
