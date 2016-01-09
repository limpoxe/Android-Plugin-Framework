package com.plugin.core.systemservice;

import android.app.ActivityManager;
import android.content.Context;

import com.plugin.core.PluginLoader;
import com.plugin.util.RefInvoker;

/**
 * Created by cailiming on 16/1/9.
 */
public class SystemServiceHook {

    /**
     * 通常系统服务实例内部都有一个成员变量private final Context mContext;
     *
     * 这个成员变量通常是一个ContextImpl实例。
     *
     * 此方法用来替换服务内部的context，来达到一些特殊目的
     *
     * 例如需要更改服务内部获取packageName、resource对象等等。
     *
     * 如果你不知道、不确定自己在干什么，请慎用此API！！！
     * 如果你不知道、不确定自己在干什么，请慎用此API！！！
     *
     * @param manager 通过getSystemService获取的系统服务。例如 ActivityManager
     *
     */
    static void replaceContext(Object manager, Context context) {
        Object original = RefInvoker.getFieldObject(manager, manager.getClass(), "mContext");
        if (original != null) {//表示确实存在此成员变量对象，替换掉
            RefInvoker.setFieldObject(manager, manager.getClass().getName(), "mContext", context);
        }
    }

}
