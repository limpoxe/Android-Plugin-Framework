package com.limpoxe.fairy.core.exception;

/**
 * Created by cailiming on 16/11/18.
 */

public class PluginNotInitError extends Error {

    public PluginNotInitError(String detailMessage) {
        super(detailMessage);
    }

    public PluginNotInitError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PluginNotInitError(Throwable throwable) {
        super(throwable);
    }
}
