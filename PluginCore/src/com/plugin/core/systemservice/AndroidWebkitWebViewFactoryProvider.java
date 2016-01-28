package com.plugin.core.systemservice;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import com.plugin.core.proxy.MethodDelegate;
import com.plugin.core.proxy.MethodProxy;
import com.plugin.core.proxy.ProxyUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cailiming on 16/1/28.
 */
public class AndroidWebkitWebViewFactoryProvider extends MethodProxy {

    public static void installProxy() {
        if (Build.VERSION.SDK_INT >= 19) {
            LogUtil.d("安装WebViewFactoryProviderProxy");
            //在4。4及以上，这里的WebViewFactoryProvider的实际类型是
            // com.android.webview.chromium.WebViewChromiumFactoryProvider implements WebViewFactoryProvider
            Object webViewFactoryProvider = RefInvoker.invokeMethod(null, "android.webkit.WebViewFactory", "getProvider", (Class[]) null, (Object[]) null);
            Object webViewFactoryProviderProxy = ProxyUtil.createProxy(webViewFactoryProvider, new AndroidWebkitWebViewFactoryProvider());
            RefInvoker.setStaticOjbect("android.webkit.WebViewFactory", "sProviderInstance", webViewFactoryProviderProxy);
            LogUtil.d("安装完成");
        }
    }

    static {
        sMethods.put("createWebView", new createWebView());
    }

    public static class createWebView extends MethodDelegate {

        @Override
        public Object afterInvoke(Object target, Method method, Object[] args, Object beforeInvoke, final Object invokeResult) {
            //这里invokeResult的实际类型是
            // com.android.webview.chromium.WebViewChromium implements WebViewProvider
            //所以这里可以再次进行Proxy
            final WebView webView = (WebView) args[0];
            return ProxyUtil.createProxy(invokeResult, new MethodDelegate() {

                @Override
                public Object beforeInvoke(Object target, Method method, Object[] args) {
                    fixWebViewAsset(webView.getContext());
                    return super.beforeInvoke(target, method, args);
                }

            });
        }
    }

    private static void fixWebViewAsset(Context context) {
        try {
            if (sContentMain == null) {
                Object provider = RefInvoker.invokeMethod(null, "android.webkit.WebViewFactory", "getProvider", (Class[]) null, (Object[]) null);
                if (provider != null) {
                    ClassLoader cl = provider.getClass().getClassLoader();

                    try {
                        sContentMain = Class.forName("org.chromium.content.app.ContentMain", true, cl);
                    } catch (ClassNotFoundException e) {
                    }

                    if (sContentMain == null) {
                        try {
                            sContentMain = Class.forName("com.android.org.chromium.content.app.ContentMain", true, cl);
                        } catch (ClassNotFoundException e) {
                        }
                    }

                    if (sContentMain == null) {
                        throw new ClassNotFoundException(String.format("Can not found class %s or %s in classloader %s", "org.chromium.content.app.ContentMain", "com.android.org.chromium.content.app.ContentMain", cl));
                    }
                }
            }
            if (sContentMain != null) {
                RefInvoker.invokeMethod(null, sContentMain, "initApplicationContext", new Class[]{Context.class}, new Object[]{context.getApplicationContext()});
            }
        } catch (Exception e) {
            LogUtil.printException("createWebview", e);
        }
    }

    private static Class sContentMain;

}