package com.limpoxe.fairy.core.localservice;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.support.servicemanager.ServiceManager;
import com.limpoxe.support.servicemanager.local.ServicePool;
import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cailiming on 16/1/1.
 */
public class LocalServiceManager {

    static boolean isSupport = false;

    static {
        try {
            Class ServiceManager = Class.forName("com.limpoxe.support.servicemanager.ServiceManager");
            isSupport = ServiceManager != null;
        } catch (ClassNotFoundException e) {
            LogUtil.e("ServiceManager was disabled");
        }
    }

    public static void init() {
        if (!isSupport) {
            return;
        }
        ServiceManager.init(FairyGlobal.getHostApplication());
    }

    public static void registerService(PluginDescriptor plugin) {
        if (!isSupport) {
            return;
        }
        HashMap<String, String> localServices = plugin.getFunctions();
        if (localServices != null) {
            Iterator<Map.Entry<String, String>> serv = localServices.entrySet().iterator();
            while (serv.hasNext()) {
                Map.Entry<String, String> entry = serv.next();
                LocalServiceManager.registerService(plugin.getPackageName(), entry.getKey(), entry.getValue());
            }
        }
    }

    public static void registerService(final String pluginId, final String serviceName, final String serviceClass) {
        if (!isSupport) {
            return;
        }
        ServiceManager.publishService(serviceName, new ServicePool.ClassProvider() {
            @Override
            public Object getServiceInstance() {

                //插件可能尚未初始化，确保使用前已经初始化
                LoadedPlugin plugin = PluginLauncher.instance().startPlugin(pluginId);
                if (plugin != null) {
                    try {
                        return plugin.pluginClassLoader.loadClass(serviceClass.split("\\|")[0]).newInstance();
                    } catch (ClassNotFoundException e) {
                        LogUtil.printException("获取服务失败", e);
                    } catch (InstantiationException e) {
                        LogUtil.printException("LocalServiceManager.registerService", e);
                    } catch (IllegalAccessException e) {
                        LogUtil.printException("LocalServiceManager.registerService", e);
                    }
                } else {
                    LogUtil.e("未找到插件", pluginId);
                }
                return null;
            }

            @Override
            public String getInterfaceName() {
                return serviceClass.split("\\|")[1];
            }
        });
    }

    public static Object getService(String name) {
        if (!isSupport) {
            return null;
        }
        return ServiceManager.getService(name);
    }

    public static void unRegistService(PluginDescriptor plugin) {
        if (!isSupport) {
            return;
        }
        HashMap<String, String> localServices = plugin.getFunctions();
        if (localServices != null) {
            Iterator<Map.Entry<String, String>> serv = localServices.entrySet().iterator();
            while (serv.hasNext()) {
                Map.Entry<String, String> entry = serv.next();
                ServiceManager.unPublishService(entry.getKey());
            }
        }
    }

    public static void unRegistAll() {
        if (!isSupport) {
            return;
        }
        ServiceManager.unPublishAllService();
    }

}
