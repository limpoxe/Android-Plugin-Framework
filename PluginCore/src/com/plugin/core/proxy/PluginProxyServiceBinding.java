package com.plugin.core.proxy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Service Bind动态绑定，解决代理bind service 无法真正Stop问题。
 */
public class PluginProxyServiceBinding {

    public static final String PROXY_SERVICE_PRE = PluginProxyService.class.getPackage().getName();

    private static final String ACTION_BIND_SERVICE = "com.plugin.core.BIND_SERVICE";

    /**
     * key:stub Activity Name
     * value:plugin Activity Name
     */
    private static HashMap<String, String> bindServiceMapping = new HashMap<String, String>();

    /**
     * 跨进程
     * key:stub Activity Name
     * value:plugin Activity Name
     */
    private static HashMap<String, String> bindProcessServiceMapping = new HashMap<String, String>();

    private static boolean isPoolInited = false;

    public static String bindProxyService(String pluginServiceClassName) {

        initPool();

        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(pluginServiceClassName);
        Iterator<Map.Entry<String, String>> itr;
        //判断是否为跨进程Service
        boolean bool = pluginDescriptor.getProcessNames().containsKey(pluginServiceClassName);
        LogUtil.d("process bool:" + bool);
        if (bool) {
            itr = bindProcessServiceMapping.entrySet().iterator();
        } else {
            itr = bindServiceMapping.entrySet().iterator();
        }

        if (itr != null) {

            String idleBindServiceName = null;

            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                if (entry.getValue() == null) {
                    if (idleBindServiceName == null) {
                        idleBindServiceName = entry.getKey();
                    }
                } else if (pluginServiceClassName.equals(entry.getValue())) {
                    LogUtil.d("bindProxyService:" + entry.getKey());
                    return entry.getKey();
                }
            }

            //没有绑定到ProxyService，而且还有空余的ProxyService，进行绑定
            if (idleBindServiceName != null) {
                if (bool) {
                    bindProcessServiceMapping.put(idleBindServiceName, pluginServiceClassName);
                } else {
                    bindServiceMapping.put(idleBindServiceName, pluginServiceClassName);
                }
                LogUtil.d("bindProxyService:" + idleBindServiceName);
                return idleBindServiceName;
            }

        }

        //绑定失败，抛出异常，没有空余的ProxyService，说明代理Service已经不够用，可以自行添加
        throw new RuntimeException("BindProxyService fail process:" + bool);
    }

    private static void initPool() {
        if (isPoolInited) {
            return;
        }

        Intent bindServiceIntent = new Intent();
        bindServiceIntent.setAction(ACTION_BIND_SERVICE);
        bindServiceIntent.setPackage(PluginLoader.getApplicatoin().getPackageName());

        List<ResolveInfo> list = PluginLoader.getApplicatoin().getPackageManager().queryIntentServices(bindServiceIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (list != null && list.size() > 0) {
            for (ResolveInfo resolveInfo :
                    list) {
                String name = resolveInfo.serviceInfo.name;
                if (name.startsWith(PROXY_SERVICE_PRE)) {
                    String processName = resolveInfo.serviceInfo.processName;
                    LogUtil.d("processName:" + processName);
                    //判断是否跨进程
                    if (!TextUtils.isEmpty(processName) && !PluginLoader.getApplicatoin().getPackageName().equals(processName)) {
                        bindProcessServiceMapping.put(name, null);
                    } else {
                        bindServiceMapping.put(name, null);
                    }

                }
            }
        }

        isPoolInited = true;
    }

    public static String unBindProxyService(String pluginServiceClassName) {
        String idleBindServiceName = null;
        PluginDescriptor pluginDescriptor = PluginLoader.getPluginDescriptorByClassName(pluginServiceClassName);
        Iterator<Map.Entry<String, String>> itr;
        //判断是否为跨进程Service
        boolean bool = pluginDescriptor.getProcessNames().containsKey(pluginServiceClassName);
        LogUtil.d("process bool:" + bool);
        if (bool) {
            itr = bindProcessServiceMapping.entrySet().iterator();
        } else {
            itr = bindServiceMapping.entrySet().iterator();
        }

        if (itr != null) {
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                if (pluginServiceClassName.equals(entry.getValue())) {
                    LogUtil.d("bindProxyService:" + entry.getKey());
                    idleBindServiceName = entry.getKey();
                    if (bool) {
                        bindProcessServiceMapping.put(idleBindServiceName, null);
                    } else {
                        bindServiceMapping.put(idleBindServiceName, null);
                    }
                }
            }
        }

        return idleBindServiceName;
    }
}
