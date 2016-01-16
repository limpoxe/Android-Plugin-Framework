package com.plugin.util;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.widget.RemoteViews;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginIntentResolver;
import com.plugin.core.PluginLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by cailiming on 16/1/10.
 */
public class NotificationHelper {
    /**
     * used before send notification
     * @param intent
     * @return
     */
    public static Intent resolveNotificationIntent(Intent intent, int type) {

        if (type == PluginDescriptor.BROADCAST) {

            Intent newIntent = PluginIntentResolver.resolveReceiver(intent).get(0);
            return newIntent;

        } else if (type == PluginDescriptor.ACTIVITY) {

            PluginIntentResolver.resolveActivity(intent);
            return intent;

        } else if (type == PluginDescriptor.SERVICE) {

            PluginIntentResolver.resolveService(intent);
            return intent;

        }
        return intent;
    }

}
