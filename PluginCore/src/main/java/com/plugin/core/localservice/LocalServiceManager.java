package com.plugin.core.localservice;

import com.limpoxe.support.servicemanager.ServiceManager;
import com.plugin.content.LoadedPlugin;
import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLauncher;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cailiming on 16/1/1.
 */
public class LocalServiceManager {

    public static void init() {
        ServiceManager.init(PluginLoader.getApplication());
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

    public static void registerService(final String pluginId, final String serviceName, final String serviceClass) {

        ServiceManager.publishService(serviceName, new com.limpoxe.support.servicemanager.local.LocalServiceManager.ClassProvider() {
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
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
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
        return ServiceManager.getService(name);
    }

    public static void unRegistService(PluginDescriptor plugin) {
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
        ServiceManager.unPublishAllService();
    }

}
