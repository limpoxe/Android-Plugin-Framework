package com.limpoxe.fairy.core.compat;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackContentProviderClient;
import com.limpoxe.fairy.core.android.HackIContentProvider;
import com.limpoxe.fairy.util.LogUtil;

/**
 * Created by cailiming on 16/4/14.
 */
public class CompatForContentProvider {

    public static Bundle call(Uri uri, String method, String arg, Bundle extras) {

        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();

        if (Build.VERSION.SDK_INT >= 11) {
            try {
                return resolver.call(uri, method, arg, extras);
            } catch (Exception e) {
                LogUtil.e("call uri fail", uri, method, arg, extras);
            }
            return null;
        } else {
            ContentProviderClient client = resolver.acquireContentProviderClient(uri);
            if (client == null) {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            try {
                HackContentProviderClient hackContentProviderClient = new HackContentProviderClient(client);
                Object mContentProvider = hackContentProviderClient.getContentProvider();
                if (mContentProvider != null) {
                    //public Bundle call(String method, String request, Bundle args)
                    Object result = new HackIContentProvider(mContentProvider).call(method, arg, extras);
                    return  (Bundle) result;
                }

            } finally {
                client.release();
            }
            return null;
        }
    }
}
