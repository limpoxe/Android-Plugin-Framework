package com.limpoxe.fairy.core.localservice;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.support.servicemanager.ServiceManager;
import com.limpoxe.support.servicemanager.local.ServicePool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
