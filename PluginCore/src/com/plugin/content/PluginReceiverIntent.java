package com.plugin.content;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by cailiming on 15/9/29.
 */
public class PluginReceiverIntent extends Intent {

    public PluginReceiverIntent(Intent o) {
        super(o);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setExtrasClassLoader(ClassLoader loader) {
        Bundle extra = getExtras();
        if (extra != null) {
            loader = extra.getClassLoader();
        }
        super.setExtrasClassLoader(loader);
    }
}