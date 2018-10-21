package com.limpoxe.fairy.core.localservice;

import com.limpoxe.fairy.content.PluginDescriptor;

/**
 * Created by cailiming on 16/1/1.
 *
 * 已废弃：2018-10-21
 */
@Deprecated
public class LocalServiceManager {

    static boolean isSupport = false;

    public static void init() {
        if (!isSupport) {
            return;
        }
    }

    public static void registerService(PluginDescriptor plugin) {
        if (!isSupport) {
            return;
        }
    }

    public static void registerService(final String pluginId, final String serviceName, final String serviceClass) {
        if (!isSupport) {
            return;
        }
    }

    public static Object getService(String name) {
        if (!isSupport) {
            return null;
        }
        return null;
    }

    public static void unRegistService(PluginDescriptor plugin) {
        if (!isSupport) {
            return;
        }
    }

    public static void unRegistAll() {
        if (!isSupport) {
            return;
        }
    }

}
