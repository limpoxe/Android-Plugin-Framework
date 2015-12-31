package com.plugin.core.localservice;

/**
 * Created by cailiming on 16/1/1.
 *
 * 所有context公用
 *
 */
public abstract class LocalServiceFetcher {
    int mServiceId;
    private Object mCachedInstance;

    public final Object getService() {
        synchronized (LocalServiceFetcher.this) {
            Object service = mCachedInstance;
            if (service != null) {
                return service;
            }
            return mCachedInstance = createService(mServiceId);
        }
    }

    public abstract Object createService(int serviceId);

}
