package com.limpoxe.fairy.core.android;

import android.content.Intent;

import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/30.
 */

public class HackReceiverData {
    private static final String ClassName = "android.app.ActivityThread$ReceiverData";

    private static final String Field_intent = "intent";

    private Object instance;

    public HackReceiverData(Object instance) {
        this.instance = instance;
    }

    public Intent getIntent() {
        return (Intent)RefInvoker.getField(instance, ClassName, Field_intent);
    }

    public void setIntent(Intent intent) {
        RefInvoker.setField(instance, ClassName, Field_intent, intent);
    }
}
