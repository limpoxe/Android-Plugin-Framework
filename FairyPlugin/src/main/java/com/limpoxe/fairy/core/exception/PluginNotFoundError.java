package com.limpoxe.fairy.core.exception;

/**
 * Created by cailiming on 16/11/18.
 */

public class PluginNotFoundError extends Error {

    public PluginNotFoundError(String detailMessage) {
        super(detailMessage);
    }

    public PluginNotFoundError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PluginNotFoundError(Throwable throwable) {
        super(throwable);
    }
}
