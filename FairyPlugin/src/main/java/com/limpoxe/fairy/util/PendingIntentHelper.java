package com.limpoxe.fairy.util;

import android.content.Intent;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginIntentResolver;

/**
 * Created by cailiming on 16/1/10.
 */
public class PendingIntentHelper {
    /**
     * used before send notification
     * @param intent
     * @return
     */
    public static Intent resolvePendingIntent(Intent intent, int type) {

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
