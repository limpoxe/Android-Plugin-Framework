package com.plugin.core.localservice;

import java.util.HashMap;

/**
 * Created by cailiming on 16/1/1.
 */
public class LocalServiceManager {

    private static final HashMap<String, LocalServiceFetcher> SYSTEM_SERVICE_MAP =
            new HashMap<String, LocalServiceFetcher>();

    private LocalServiceManager() {
    }

    public static void registerService(String serviceName, LocalServiceFetcher fetcher) {
        fetcher.mServiceId ++;
        SYSTEM_SERVICE_MAP.put(serviceName, fetcher);
    }

    public static Object getService(String name) {
        LocalServiceFetcher fetcher = SYSTEM_SERVICE_MAP.get(name);
        return fetcher == null ? null : fetcher.getService();
    }

}
