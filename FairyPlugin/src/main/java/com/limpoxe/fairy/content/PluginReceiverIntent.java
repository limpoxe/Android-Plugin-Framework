package com.limpoxe.fairy.content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by cailiming on 15/9/29.
 */
@SuppressLint("ParcelCreator")
public class PluginReceiverIntent extends Intent {

    public PluginReceiverIntent(Intent o) {
        super(o);
    }

    @Override
    public void setExtrasClassLoader(ClassLoader loader) {
        if (Build.VERSION.SDK_INT > 11) {
            Bundle extra = getExtras();
            if (extra != null) {
                loader = extra.getClassLoader();
            }
        }
        super.setExtrasClassLoader(loader);
    }
}