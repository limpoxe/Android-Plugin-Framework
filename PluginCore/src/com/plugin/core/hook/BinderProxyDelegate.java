package com.plugin.core.hook;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.core.manager.PluginManagerHelper;
import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.util.LogUtil;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/7/14.
 */
public class BinderProxyDelegate extends MethodDelegate {

    private final String descriptor;

    public BinderProxyDelegate(String descriptor) {
        this.descriptor = descriptor;
    }

    public Object beforeInvoke(Object target, Method method, Object[] args) {
        LogUtil.d("beforeInvoke", descriptor, method.getName());

        //这里做此判定是为了把一些特定的接口方法仍然交给特定的MethodProxy去处理,不在此做统一处理
        //这些"特定的MethodProxy"主要是一些查询类接口
        if(!MethodProxy.sMethods.containsKey(method.getName())) {

            if (method.getName().equals("relayout")) {
                for(Object o : args) {
                    LogUtil.d(o);
                }
            }

            replacePackageName(args);
        }
        return null;
    }

    public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, Object invokeResult) {
        if (beforeInvoke != null) {
            return beforeInvoke;
        }
        return invokeResult;
    }

    private void replacePackageName(Object[] args) {
        if(args != null && args.length>0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof  String && ((String)args[i]).contains(".")) {
                    //包含.号,基本可以判定是packageName
                    PluginDescriptor pd = PluginManagerHelper.getPluginDescriptorByPluginId((String)args[i]);
                    if(pd != null) {
                        //说明传的是插件包名, 修正为宿主包名
                        args[i] = PluginLoader.getApplication().getPackageName();

                        //这里或许需要break,提高效率?
                        //break;
                    }
                }
            }
        }
    }
}
