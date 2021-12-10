package com.limpoxe.fairy.core;

import android.app.Application;

import com.limpoxe.fairy.core.exception.PluginResInitError;
import com.limpoxe.fairy.manager.mapping.StubMappingProcessor;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import java.util.ArrayList;

public class FairyGlobal {
    private static boolean sIsInited;
    private static Application sApplication;
    private static boolean sIsLocalHtmlEnable = false;
    private static int sLoadingResId;
    private static long sMinLoadingTime = 400;
    private static boolean sIsNeedVerifyPluginSign = true;
    private static boolean sSupportRemoteViews = true;
    private static ArrayList<StubMappingProcessor> mappingProcessors = new ArrayList<StubMappingProcessor>();
    private static boolean sFakePluginProcessName = true;
    private static boolean sNeedVerifyHostVersionName = true;
    private static String sNotificationResPath;
    private static boolean sInstallationWithSameVersion = false;

    public static Application getHostApplication() {
        if (sApplication == null) {
            throw new IllegalStateException("not inited yet");
        }
        return sApplication;
    }

    /*package*/ static void setApplication(Application application) {
        sApplication = application;
        //public_static_final_host_string_这个资源是由框架脚本植入宿主的, 由此判断对宿主的id分组是否成功
        int id = sApplication.getResources().getIdentifier("public_static_final_host_string_", "string", sApplication.getPackageName());
        if (id != 0 && !ResourceUtil.isMainResId(id)) {
            throw new PluginResInitError("对宿主id分组失败，说明宿主编译有错，请clean后重新编译: " + ResourceUtil.covent2Hex(String.valueOf(id)));
        }
    }

    /*package*/ static void setIsInited(boolean isInited) {
        sIsInited = isInited;
    }

    public static boolean isInited() {
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

    /**
     * 控制框架日志是否打印
     * @param isLogEnable
     */
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
     * 时间设置为0表示无loading页， 默认400ms
     * @param minLoadingTime
     */
    public static void setMinLoadingTime(long minLoadingTime) {
        sMinLoadingTime = minLoadingTime;
    }

    public static long getMinLoadingTime() {
        return sMinLoadingTime;
    }

    /**
     * 是否需要验证"插件和宿主的签名相同"
     * @param needVerify
     */
    public static void setNeedVerifyPlugin(boolean needVerify) {
        sIsNeedVerifyPluginSign = needVerify;
    }

    public static boolean isNeedVerifyPlugin() {
        return sIsNeedVerifyPluginSign;
    }

    public static boolean isNeedVerifyHostVersionName() {
        return sNeedVerifyHostVersionName;
    }

    public static void setNeedVerifyHostVersionName(boolean needVerify) {
        sNeedVerifyHostVersionName = needVerify;
    }

    public static String getNotificationResPath() {
        if (sNotificationResPath == null) {
            return getHostApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk";
        }
        return sNotificationResPath;
    }

    public static void setNotificationResPath(String notificationResPath) {
        sNotificationResPath = notificationResPath;
    }

    /**
     * 如果两个processor可以处理同一个映射关系，则后添加processor生效，先添加的processor会被忽略
     * @param processor
     */
    public static void registStubMappingProcessor(StubMappingProcessor processor) {
        if (processor == null) {
            return;
        }
        if (mappingProcessors == null) {
            mappingProcessors = new ArrayList<StubMappingProcessor>();
        }
        if (!mappingProcessors.contains(processor)) {
            mappingProcessors.add(processor);
        }
    }

    public static ArrayList<StubMappingProcessor> getStubMappingProcessors() {
        return mappingProcessors;
    }

    /**
     * 是否需要支持插件中发送notification是使用remoteviews并携带插件资源
     * @return
     */
    public static boolean isSupportRemoteViews() {
        return sSupportRemoteViews;
    }

    public static void setSupportRemoteViews(boolean support){
        sSupportRemoteViews = support;
    }

    public static boolean isFakePluginProcessName() {
        return sFakePluginProcessName;
    }

    /**
     * 是否需要伪造插件进程名称，使得在插件中通过getRunningProcesses来判断当前进程时，返回的是宿主主进程而不是插件进程
     * @param fake
     */
    public static void setFakePluginProcessName(boolean fake) {
        sFakePluginProcessName = fake;
    }

    /**
     * 安装插件的时候，如果已经安装的版本，和将要安装的版本相同时，是否允许安装
     * 默认不允许
     * @return
     */
    public static boolean isInstallationWithSameVersion() {
        return sInstallationWithSameVersion;
    }

    public static void setInstallationWithSameVersion(boolean withSameVersion) {
        sInstallationWithSameVersion = withSameVersion;
    }
}
