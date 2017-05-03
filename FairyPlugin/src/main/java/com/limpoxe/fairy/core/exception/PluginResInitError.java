package com.limpoxe.fairy.core.exception;

/**
 * Created by cailiming on 16/11/18.
 */

public class PluginResInitError extends Error {

    public PluginResInitError(String detailMessage) {
        super(detailMessage);
    }

    public PluginResInitError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PluginResInitError(Throwable throwable) {
        super(throwable);
    }
}
