package com.limpoxe.fairy.core.viewfactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/12/13.
 *
 * Porting From SupportV7
 */
public class PluginFactoryCompat {
    private static final String TAG = "FactoryCompat";

    private static Field sLayoutInflaterFactory2Field;
    private static boolean sCheckedField;

    static class FactoryWrapper implements LayoutInflater.Factory {
        final PluginFactoryInterface mDelegateFactory;

        FactoryWrapper(PluginFactoryInterface delegateFactory) {
            mDelegateFactory = delegateFactory;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return mDelegateFactory.onCreateView(null, name, context, attrs);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static class FactoryWrapper2 extends FactoryWrapper implements LayoutInflater.Factory2 {

        FactoryWrapper2(PluginFactoryInterface delegateFactory) {
            super(delegateFactory);
        }

        @Override
        public View onCreateView(View parent, String name, Context context,
                                 AttributeSet attributeSet) {
            return mDelegateFactory.onCreateView(parent, name, context, attributeSet);
        }
    }

    static void setFactory(LayoutInflater inflater, PluginFactoryInterface factory) {
        if (Build.VERSION.SDK_INT >=11) {
            final LayoutInflater.Factory2 factory2 = factory != null
                    ? new FactoryWrapper2(factory) : null;
            inflater.setFactory2(factory2);

            if (Build.VERSION.SDK_INT < 21) {
                final LayoutInflater.Factory f = inflater.getFactory();
                if (f instanceof LayoutInflater.Factory2) {
                    // The merged factory is now set to getFactory(), but not getFactory2() (pre-v21).
                    // We will now try and force set the merged factory to mFactory2
                    forceSetFactory2(inflater, (LayoutInflater.Factory2) f);
                } else {
                    // Else, we will force set the original wrapped Factory2
                    forceSetFactory2(inflater, factory2);
                }
            }
        } else {
            final LayoutInflater.Factory factory1 = factory != null
                    ? new FactoryWrapper(factory) : null;
            inflater.setFactory(factory1);
        }

    }

    /**
     * For APIs >= 11 && < 21, there was a framework bug that prevented a LayoutInflater's
     * Factory2 from being merged properly if set after a cloneInContext from a LayoutInflater
     * that already had a Factory2 registered. We work around that bug here. If we can't we
     * log an error.
     */
    static void forceSetFactory2(LayoutInflater inflater, LayoutInflater.Factory2 factory) {
        if (!sCheckedField) {
            try {
                sLayoutInflaterFactory2Field = LayoutInflater.class.getDeclaredField("mFactory2");
                sLayoutInflaterFactory2Field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "forceSetFactory2 Could not find field 'mFactory2' on class "
                        + LayoutInflater.class.getName()
                        + "; inflation may have unexpected results.", e);
            }
            sCheckedField = true;
        }
        if (sLayoutInflaterFactory2Field != null) {
            try {
                sLayoutInflaterFactory2Field.set(inflater, factory);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "forceSetFactory2 could not set the Factory2 on LayoutInflater "
                        + inflater + "; inflation may have unexpected results.", e);
            }
        }
    }
}
