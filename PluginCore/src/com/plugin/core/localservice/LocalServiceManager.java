package com.plugin.core.localservice;

import com.plugin.util.LogUtil;

import java.util.HashMap;

/**
 * Created by cailiming on 16/1/1.
 */
public class LocalServiceManager {

    private static final HashMap<String, LocalServiceFetcher> SYSTEM_SERVICE_MAP =
            new HashMap<String, LocalServiceFetcher>();

    private LocalServiceManager() {
    }

    public static void registerService(String serviceName, final Object object) {
        LocalServiceFetcher fetcher = new LocalServiceFetcher() {
            @Override
            public Object createService(int serviceId) {
                return object;
            }
        };
        fetcher.mServiceId ++;
        SYSTEM_SERVICE_MAP.put(serviceName, fetcher);
        LogUtil.d("registerService", serviceName);
    }

    public static Object getService(String name) {
        LocalServiceFetcher fetcher = SYSTEM_SERVICE_MAP.get(name);
        LogUtil.d("getService", fetcher == null?"No fetcher":"fetcher found");
        return fetcher == null ? null : fetcher.getService();
    }

    protected static abstract class LocalServiceFetcher {
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


}
