package com.limpoxe.fairy.core.compat;

import android.content.pm.ResolveInfo;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by cailiming on 16/10/22.
 */

public class CompatForParceledListSliceApi21 {
    private static Constructor sConstructor;

    public CompatForParceledListSliceApi21() {
    }

    public static Object newInstance(List<ResolveInfo> itemList) {
        Object result = null;

        try {
            result = sConstructor.newInstance(new Object[]{itemList});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    static {
        try {
            Class e = Class.forName("android.content.pm.ParceledListSlice");
            sConstructor = e.getConstructor(new Class[]{List.class});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
