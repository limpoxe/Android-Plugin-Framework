package com.plugin.core.systemservice;

import android.view.LayoutInflater;
import android.view.View;

import com.plugin.util.RefInvoker;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class AndroidViewLayoutInflater {

    private static final String android_view_LayoutInflater_sConstructorMap = "sConstructorMap";

    private static final HashMap<String, Constructor<? extends View>> sConstructorMap =
            new HashMap<String, Constructor<? extends View>>();
    /**
     * 添加这个方法的原因是，LayoutInflater类中有一个HashMap类型的全局静态View构造器缓存
     * 这个缓存在下列场景下会引起一个问题：
     * 1、插件是独立插件，包含supportV7包，则会包含一系列自定义View，比如android.support.v7.internal.widget.ActionBarOverlayLayout
     * 2、宿主也包含supportV7包，    则也会包含一系列自定义View，   比如android.support.v7.internal.widget.ActionBarOverlayLayout
     * 3、打开宿主的一个supportv7页面时，会通过LayoutInflater构造上述自定义控件，
     *    此时LayoutInflater会先查找缓存的控件构造器，没有找到，再通过宿主classloader加载控件的构造器。再将构造器缓存。
     * 4、再打开插件的一个supportv7页面，会通过LayoutInflater构造上述自定义控件，
     *    此时LayoutInflater会先查找缓存的控件构造器，如果没有找到，再通过插件classloader加载控件的构造器。再将构造器缓存。
     *    但是！！此时在缓存中查找构造器时，必然会找到宿主缓存的构造器。然后就会构造出一个来自宿主的同名自定义控件。
     *    导致插件中包含的同名自定义控件被屏蔽了。
     *
     */
    public static void installPluginCustomViewConstructorCache() {
        Map cache = (Map)RefInvoker.getFieldObject(null, LayoutInflater.class, android_view_LayoutInflater_sConstructorMap);
        if (cache != null) {
            ConstructorHashMap<String, Constructor<? extends View>> newCacheMap = new ConstructorHashMap<String, Constructor<? extends View>>();
            newCacheMap.putAll(cache);
            RefInvoker.setFieldObject(null, LayoutInflater.class, android_view_LayoutInflater_sConstructorMap, newCacheMap);
        }
    }

    public static class ConstructorHashMap<K, V> extends HashMap<K, V> {

        @Override
        public V put(K key, V value) {
            if (systemClassloader == null) {
                systemClassloader = AndroidViewLayoutInflater.class.getClassLoader().getParent();
            }
            Constructor<? extends View> constructor = (Constructor<? extends View>)value;
            // 如果是系统控件，才缓存。如果是自定义控件，无论是来自插件还是来自宿主，都不缓存
            // 如果app里面使用大量自定义控件，可能会稍微影响效率
            if (constructor.getDeclaringClass().getClassLoader() == systemClassloader) {
                return super.put(key, value);
            } else {
                return super.put(key, null);
            }

        }

    }

    private static ClassLoader systemClassloader;
}
