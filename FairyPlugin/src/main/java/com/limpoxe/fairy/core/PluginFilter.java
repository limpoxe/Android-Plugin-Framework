package com.limpoxe.fairy.core;

public class PluginFilter {

    public static boolean maybePlugin(String pluginId) {
        if (pluginId.startsWith("com.android.")
            || pluginId.startsWith("com.google.")) {
            return false;
        }
        return true;
    }

}
