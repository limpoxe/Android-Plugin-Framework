package com.limpoxe.fairy.core.proxy.systemservice;

import android.content.Context;
import android.os.Build;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackToast;
import com.limpoxe.fairy.core.proxy.MethodDelegate;
import com.limpoxe.fairy.core.proxy.MethodProxy;
import com.limpoxe.fairy.core.proxy.ProxyUtil;
import com.limpoxe.fairy.util.FakeUtil;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.lang.reflect.Method;

/**
 * Created by cailiming on 21/4/24.
 */
public class AndroidWidgetToast extends MethodProxy {

    static {
        if (Build.VERSION.SDK_INT >= 30) {
            sMethods.put("enqueueToast", new enqueueToast());
            sMethods.put("cancelToast", new cancelToast());
            sMethods.put("finishToken", new finishToken());
        }
    }

    public static void installProxy() {
        if (Build.VERSION.SDK_INT >= 30) {
            LogUtil.d("安装AndroidWidgetToastProxy");
            Object androidAppINotificationStubProxy = HackToast.getService();
            Object androidAppINotificationStubProxyProxy = ProxyUtil.createProxy(androidAppINotificationStubProxy, new AndroidWidgetToast());
            HackToast.setService(androidAppINotificationStubProxyProxy);
            LogUtil.d("安装完成");
        }
    }

    public static class enqueueToast extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName());
            Object tn = args[2];
            //TN
            RefInvoker.setField(tn, tn.getClass(), "mPackageName", FairyGlobal.getHostApplication().getPackageName());
            Object mPresenter = RefInvoker.getField(tn, tn.getClass(), "mPresenter");
            if (mPresenter != null) {
                //ToastPresenter
                RefInvoker.setField(mPresenter, mPresenter.getClass(), "mPackageName", FairyGlobal.getHostApplication().getPackageName());
                Context mContext = (Context)RefInvoker.getField(mPresenter, mPresenter.getClass(), "mContext");
                if (mContext != null) {
                    RefInvoker.setField(mPresenter, mPresenter.getClass(), "mContext", FakeUtil.fakeContext(mContext));
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class cancelToast extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName(), args[0]);
            args[0] = FairyGlobal.getHostApplication().getPackageName();
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class finishToken extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            LogUtil.v("beforeInvoke", method.getName(), args[0]);
            args[0] = FairyGlobal.getHostApplication().getPackageName();
            return super.beforeInvoke(target, method, args);
        }
    }

}
