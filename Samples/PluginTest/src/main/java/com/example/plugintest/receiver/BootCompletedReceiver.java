package com.example.plugintest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.limpoxe.fairy.util.LogUtil;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.e("BootCompletedReceiver.onReceive", intent);
    }
}
