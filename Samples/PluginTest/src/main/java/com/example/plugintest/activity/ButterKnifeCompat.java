package com.example.plugintest.activity;

import android.app.Activity;
import android.app.Dialog;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by cailiming on 2017/5/12.
 */

public class ButterKnifeCompat {

    static final Map<Class<?>, Constructor<? extends Unbinder>> BINDINGS = new LinkedHashMap();

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Activity target) {
        Unbinder unbinder = ButterKnife.bind(target);
        if (unbinder == Unbinder.EMPTY) {
            View sourceView = target.getWindow().getDecorView();
            return createBinding(target, sourceView);
        } else {
            return unbinder;
        }
    }

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull View target) {
        Unbinder unbinder = ButterKnife.bind(target);
        if (unbinder == Unbinder.EMPTY) {
            return createBinding(target, target);
        } else {
            return unbinder;
        }
    }

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Dialog target) {
        Unbinder unbinder = ButterKnife.bind(target);
        if (unbinder == Unbinder.EMPTY) {
            View sourceView = target.getWindow().getDecorView();
            return createBinding(target, sourceView);
        } else {
            return unbinder;
        }
    }

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Object target, @NonNull Activity source) {
        Unbinder unbinder = ButterKnife.bind(target, source);
        if (unbinder == Unbinder.EMPTY) {
            View sourceView = source.getWindow().getDecorView();
            return createBinding(target, sourceView);
        } else {
            return unbinder;
        }
    }

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Object target, @NonNull View source) {
        Unbinder unbinder = ButterKnife.bind(target, source);
        if (unbinder == Unbinder.EMPTY) {
            return createBinding(target, source);
        } else {
            return unbinder;
        }
    }

    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Object target, @NonNull Dialog source) {
        Unbinder unbinder = ButterKnife.bind(target, source);
        if (unbinder == Unbinder.EMPTY) {
            View sourceView = source.getWindow().getDecorView();
            return createBinding(target, sourceView);
        } else {
            return unbinder;
        }
    }

    private static Unbinder createBinding(@NonNull Object target, @NonNull View source) {
        Class targetClass = target.getClass();
        Constructor constructor = findBindingConstructorForClass(targetClass);
        if(constructor == null) {
            return Unbinder.EMPTY;
        } else {
            try {
                return (Unbinder)constructor.newInstance(new Object[]{target, source});
            } catch (IllegalAccessException var6) {
                throw new RuntimeException("Unable to invoke " + constructor, var6);
            } catch (InstantiationException var7) {
                throw new RuntimeException("Unable to invoke " + constructor, var7);
            } catch (InvocationTargetException var8) {
                Throwable cause = var8.getCause();
                if(cause instanceof RuntimeException) {
                    throw (RuntimeException)cause;
                } else if(cause instanceof Error) {
                    throw (Error)cause;
                } else {
                    throw new RuntimeException("Unable to create binding instance.", cause);
                }
            }
        }
    }

    @Nullable
    @CheckResult
    @UiThread
    private static Constructor<? extends Unbinder> findBindingConstructorForClass(Class<?> cls) {
        Constructor bindingCtor = (Constructor)BINDINGS.get(cls);
        if(bindingCtor != null) {
            return bindingCtor;
        } else {
            String clsName = cls.getName();
            if(!clsName.startsWith("android.") && !clsName.startsWith("java.")) {
                try {
                    Class e = Class.forName(clsName + "_ViewBinding", true, cls.getClassLoader());
                    bindingCtor = e.getConstructor(new Class[]{cls, View.class});
                } catch (ClassNotFoundException var4) {
                    bindingCtor = findBindingConstructorForClass(cls.getSuperclass());
                } catch (NoSuchMethodException var5) {
                    throw new RuntimeException("Unable to find binding constructor for " + clsName, var5);
                }
                BINDINGS.put(cls, bindingCtor);
                return bindingCtor;
            } else {
                return null;
            }
        }
    }

}
