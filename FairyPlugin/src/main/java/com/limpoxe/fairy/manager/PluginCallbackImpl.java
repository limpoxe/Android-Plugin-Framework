package com.limpoxe.fairy.manager;

import android.content.Intent;

import com.limpoxe.fairy.core.FairyGlobal;

/**
 * Created by cailiming on 2015/9/13.
 */
public class PluginCallbackImpl implements PluginStatusChangeListener {

    @Override
    public void onInstall(int result, String packageName, String version,  String src) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.setPackage(FairyGlobal.getHostApplication().getPackageName());
        intent.putExtra(EXTRA_TYPE, TYPE_INSTALL);
        intent.putExtra(EXTRA_ID, packageName);
        intent.putExtra(EXTRA_VERSION, version);
        intent.putExtra(EXTRA_RESULT_CODE, result);
        intent.putExtra(EXTRA_SRC, src);
        FairyGlobal.getHostApplication().sendBroadcast(intent);
    }

    @Override
    public void onRemove(String packageName, int code) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.setPackage(FairyGlobal.getHostApplication().getPackageName());
        intent.putExtra(EXTRA_TYPE, TYPE_REMOVE);
        intent.putExtra(EXTRA_ID, packageName);
        intent.putExtra(EXTRA_RESULT_CODE, code);
        FairyGlobal.getHostApplication().sendBroadcast(intent);
    }

    //暂未使用，有需要再加
    @Override
    public void onStart(String packageName) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.setPackage(FairyGlobal.getHostApplication().getPackageName());
        intent.putExtra(EXTRA_TYPE, TYPE_START);
        intent.putExtra(EXTRA_ID, packageName);
        FairyGlobal.getHostApplication().sendBroadcast(intent);
    }

    //暂未使用，有需要再加
    @Override
    public void onStop(String packageName) {
        Intent intent = new Intent(ACTION_PLUGIN_CHANGED);
        intent.setPackage(FairyGlobal.getHostApplication().getPackageName());
        intent.putExtra(EXTRA_TYPE, TYPE_STOP);
        intent.putExtra(EXTRA_ID, packageName);
        FairyGlobal.getHostApplication().sendBroadcast(intent);
    }

}
