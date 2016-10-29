package com.limpoxe.fairy.core.compat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

import com.limpoxe.fairy.util.RefInvoker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by cailiming on 16/4/14.
 */
public class CompatForSupportv7_23_2 {

    /**
     *  supportv7的23.2的版本中，AppCompatActivity这个类重写了getResource方法，返回了一个TintResources的对象
     *  这里需要特别处理一下, 否者接下来的setTheme方法会导致crash
     *  其他版本，包括更低和更高版本的AppCompatActivity都没有重写这个方法.
     *  <Pre>
     *       public Resources getResources() {
     *           if (mResources == null) {
     *               mResources = new TintResources(this, super.getResources());
     *           }
     *           return mResources;
     *       }
     *  </Pre>
     * @param pluginContext
     * @param activity
     */
    public static void fixResource(Context pluginContext, Activity activity) {
        try {
            Class AppCompatActivity = pluginContext.getClassLoader().loadClass("android.support.v7.app.AppCompatActivity");
            if (AppCompatActivity != null && AppCompatActivity.isAssignableFrom(activity.getClass())) {
                //判断Activity的getResource的类型是否为TintResources
                Resources activiyResource = activity.getResources();
                Class TintResources = pluginContext.getClassLoader().loadClass("android.support.v7.widget.TintResources");
                if (TintResources != null && TintResources.isAssignableFrom(activiyResource.getClass())) {
                    RefInvoker.setField(activity, AppCompatActivity, "mResources", pluginContext.getResources());
                    Class TintContextWrapper = pluginContext.getClassLoader().loadClass("android.support.v7.widget.TintContextWrapper");
                    if (TintContextWrapper != null) {
                        Object sCache = (Object)RefInvoker.getField(null, TintContextWrapper, "sCache");
                        if (!(sCache instanceof TintContextWrapperArrayList)) {
                            RefInvoker.setField(null, TintContextWrapper, "sCache", new TintContextWrapperArrayList(TintContextWrapper));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //nothing
        }
    }

    public static class TintContextWrapperArrayList<V> extends ArrayList<V> {

        private Class TintContextWrapper;

        public TintContextWrapperArrayList(Class TintContextWrapper) {
            this.TintContextWrapper = TintContextWrapper;
        }

        @Override
        public boolean add(V object) {
            WeakReference ref = (WeakReference)object;
            Object tintContextWrapper = ref.get();
            if (tintContextWrapper != null) {
                Resources resources = ((ContextWrapper)tintContextWrapper).getBaseContext().getResources();
                RefInvoker.setField(tintContextWrapper, TintContextWrapper, "mResources", resources);
                Resources.Theme theme = resources.newTheme();
                theme.setTo(((ContextWrapper)tintContextWrapper).getBaseContext().getTheme());
                RefInvoker.setField(tintContextWrapper, TintContextWrapper, "mTheme", theme);
            }
            return super.add(object);
        }
    }
}
