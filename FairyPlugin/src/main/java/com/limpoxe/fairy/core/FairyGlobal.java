package com.limpoxe.fairy.core;

import android.app.Application;

import com.limpoxe.fairy.util.LogUtil;

public class FairyGlobal {
    private static Application sApplication;
    private static boolean sIsLocalHtmlEnable;
    private static int sLoadingResId;
    private static long sMinLoadingTime = 400;

    public static Application getApplication() {
        if (sApplication == null) {
            throw new IllegalStateException("application not set！");
        }
        return sApplication;
    }

    /*package*/ static void setApplication(Application application) {
        sApplication = application;
    }

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
