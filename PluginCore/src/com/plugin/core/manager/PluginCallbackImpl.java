package com.plugin.core.manager;

import android.content.Intent;

import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;

/**
 * Created by Administrator on 2015/9/13.
 */
public class PluginCallbackImpl implements PluginCallback {

    private static final String ACTION_PLUGIN_CHANGED = "com.plugin.core.action_plugin_changed";

    @Override
    public void onPluginLoaderInited() {
        LogUtil.d("PluginLoader inited");
    }

    @Override
    public void onPluginInstalled(String packageName, String version) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.putExtra("type", "install");
        intent.putExtra("id", packageName);
        intent.putExtra("version", version);
        PluginLoader.getApplicatoin().sendBroadcast(intent);
    }

    @Override
    public void onPluginRemoved(String packageName) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.putExtra("type", "remove");
        intent.putExtra("id", packageName);
        PluginLoader.getApplicatoin().sendBroadcast(intent);
    }

    @Override
    public void onPluginStarted(String packageName) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.putExtra("type", "init");
        intent.putExtra("id", packageName);
        PluginLoader.getApplicatoin().sendBroadcast(intent);
    }

    @Override
    public void onPluginRemoveAll() {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.putExtra("type", "remove_all");
        PluginLoader.getApplicatoin().sendBroadcast(intent);
    }

}
