package com.plugin.core.localservice;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cailiming on 16/1/1.
 */
public class LocalServiceManager {

    private static final HashMap<String, LocalServiceFetcher> SYSTEM_SERVICE_MAP =
            new HashMap<String, LocalServiceFetcher>();

    private LocalServiceManager() {
    }

    public static void registerService(PluginDescriptor plugin) {
        HashMap<String, String> localServices = plugin.getFunctions();
        if (localServices != null) {
            Iterator<Map.Entry<String, String>> serv = localServices.entrySet().iterator();
            while (serv.hasNext()) {
                Map.Entry<String, String> entry = serv.next();
                LocalServiceManager.registerService(plugin.getPackageName(), entry.getKey(), entry.getValue());
            }
        }
    }

    public static void registerService(final String pluginId, String serviceName, final String serviceClass) {
        if (!SYSTEM_SERVICE_MAP.containsKey(serviceName)) {
            LocalServiceFetcher fetcher = new LocalServiceFetcher() {
                @Override
                public Object createService(int serviceId) {
                    mPluginId = pluginId;
                    PluginDescriptor pd = PluginLoader.initPluginByPluginId(pluginId);
                    if (pd != null) {
                        try {
                            Class clazz = pd.getPluginClassLoader().loadClass(serviceClass);
                            return clazz.newInstance();
                        } catch (Exception e) {
                            LogUtil.printException("获取服务失败", e);
                        }
                    } else {
                        LogUtil.e("PluginClassLoader", "未找到插件", pluginId);
                    }
                    return null;
                }
            };
            fetcher.mServiceId ++;
            SYSTEM_SERVICE_MAP.put(serviceName, fetcher);
            LogUtil.d("registerService", serviceName);
        } else {
            LogUtil.e("已注册", serviceName);
        }
    }

    public static Object getService(String name) {
        LocalServiceFetcher fetcher = SYSTEM_SERVICE_MAP.get(name);
        return fetcher == null ? null : fetcher.getService();
    }

}
