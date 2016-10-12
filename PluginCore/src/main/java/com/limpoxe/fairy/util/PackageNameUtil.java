package com.limpoxe.fairy.util;

import android.content.Context;
import android.content.ContextWrapper;

import com.limpoxe.fairy.core.PluginLoader;

public class PackageNameUtil {

    /**
     * 由于插件的getPackageName返回的是插件包名
     * 实际应用中一些第三方库可能需要使用宿主包名, 此时可以通过此方法
     * 对插件的Context的包名进行修正
     * @param context
     * @return
     */
    public static Context fakeContext(Context context) {
        if (!context.getPackageName().equals(PluginLoader.getApplication().getPackageName())) {
            context = new ContextWrapper(context) {
                @Override
                public String getPackageName() {
                    return PluginLoader.getApplication().getPackageName();
                }
            };
        }
        return context;
    }
}
