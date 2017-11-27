package com.limpoxe.fairy.core.viewfactory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/13.
 *
 * Porting From SupportV7
 */
public class PluginViewInflater {

    final Class<?>[] sConstructorSignature = new Class[] {
            Context.class, AttributeSet.class};

    private final Map<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();

    private final LayoutInflater.Factory mViewfactory;
    private final Context mContext;
    private final Object[] mConstructorArgs = new Object[2];

    public PluginViewInflater(Context context, final LayoutInflater.Factory viewfactory) {
        mContext = context;
        mViewfactory = viewfactory;
    }

    public final View createView(View parent, final String name, Context context,
                                 AttributeSet attrs, boolean inheritContext, boolean themeContext) {
        final Context originalContext = context;

        // We can emulate Lollipop's android:theme attribute propagating down the view hierarchy
        // by using the parent's context
        if (inheritContext && parent != null) {
            context = parent.getContext();
        }
        if (themeContext) {
            // We then apply the theme on the context, if specified
            context = themifyContext(context, attrs, true, true);
        }

        // We need to 'inject' our tint aware Views in place of the standard framework versions
        View view = injectView(name, context, attrs);
        if (view != null) {
            return view;
        }

        if (originalContext != context) {
            // If the original context does not equal our themed context, then we need to manually
            // inflate it using the name so that app:theme takes effect.
            return createViewFromTag(context, name, attrs);
        }

        return null;
    }

    private View injectView(String name, Context context, AttributeSet attrs) {
        if (mViewfactory != null) {
            return mViewfactory.onCreateView(name, context, attrs);
        }
        return null;
    };

    private View createViewFromTag(Context context, String name, AttributeSet attrs) {
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }

        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;

            if (-1 == name.indexOf('.')) {
                // try the android.widget prefix first...
                return createView(name, "android.widget.");
            } else {
                return createView(name, null);
            }
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        } finally {
            // Don't retain static reference on context.
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
    }

    private View createView(String name, String prefix)
            throws ClassNotFoundException, InflateException {
        Constructor<? extends View> constructor = sConstructorMap.get(name);

        try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                Class<? extends View> clazz = getClassLoader(mContext, name, prefix).loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        }
    }

    private ClassLoader getClassLoader(Context context, String name, String prefix) {
        return context.getClassLoader();
    }

    /**
     * Allows us to emulate the {@code android:theme} attribute for devices before L.
     */
    public static Context themifyContext(Context context, AttributeSet attrs,
                                         boolean useAndroidTheme, boolean useAppTheme) {
        //wrap it in a new wrapper
        //context = new ContextThemeWrapper(context, themeId);
        return context;
    }

}
