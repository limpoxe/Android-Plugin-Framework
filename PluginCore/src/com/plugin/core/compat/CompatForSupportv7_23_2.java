package com.plugin.core.compat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.View;

import com.plugin.util.RefInvoker;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cailiming on 16/4/14.
 */
public class CompatForSupportv7_23_2 {

    /**
     *  //supportv7的23.2的版本中，AppCompatActivity内部重写了getResource方法，这里需要特别处理一下, 否者接下来的setTheme方法会导致crash
     *   //其他版本，包括更低和更高版本的AppCompatActivity都不存在这个问题
     * @param pluginContext
     * @param activity
     */
    public static void fixResource(Context pluginContext, Activity activity) {
        try {
            //只有23.2的版本中存在此类
            Class TintResources = pluginContext.getClassLoader().loadClass("android.support.v7.widget.TintResources");
            if (TintResources != null) {
                Class AppCompatActivity = pluginContext.getClassLoader().loadClass("android.support.v7.app.AppCompatActivity");
                if (AppCompatActivity != null && AppCompatActivity.isAssignableFrom(activity.getClass())) {
                    RefInvoker.setFieldObject(activity, AppCompatActivity, "mResources", pluginContext.getResources());

                    Class TintContextWrapper = pluginContext.getClassLoader().loadClass("android.support.v7.widget.TintContextWrapper");
                    if (TintContextWrapper != null) {
                        Object sCache = (Object)RefInvoker.getFieldObject(null, TintContextWrapper, "sCache");
                        if (!(sCache instanceof TintContextWrapperArrayList)) {
                            RefInvoker.setFieldObject(null, TintContextWrapper, "sCache",
                                    new TintContextWrapperArrayList(TintContextWrapper, pluginContext.getResources()));
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
        private Resources resources;

        public TintContextWrapperArrayList(Class TintContextWrapper, Resources resources) {
            this.TintContextWrapper = TintContextWrapper;
            this.resources = resources;
        }

        @Override
        public boolean add(V object) {
            WeakReference ref = (WeakReference)object;
            Object tintContextWrapper = ref.get();
            if (tintContextWrapper != null) {
                RefInvoker.setFieldObject(tintContextWrapper, TintContextWrapper, "mResources", resources);
                Resources.Theme theme = resources.newTheme();
                theme.setTo(((ContextWrapper)tintContextWrapper).getBaseContext().getTheme());
                RefInvoker.setFieldObject(tintContextWrapper, TintContextWrapper, "mTheme", theme);
            }
            return super.add(object);
        }
    }
}
