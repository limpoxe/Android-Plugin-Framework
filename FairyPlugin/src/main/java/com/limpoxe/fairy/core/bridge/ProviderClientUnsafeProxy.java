package com.limpoxe.fairy.core.bridge;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.FileNotFoundException;

import static com.limpoxe.fairy.manager.PluginManifestParser.PREVIOUS;

public class ProviderClientUnsafeProxy extends ContentProvider {
    /**
     * @see com.limpoxe.fairy.core.proxy.systemservice.AndroidAppIActivityManager.getContentProvider
     */
    static final String TARGET_URL = "targetUrl";

    private ProviderInfo mProviderInfo;
    private String mAuthority = null;

    public ProviderClientUnsafeProxy() {
        Log.d("ProviderUnsafeProxy", "create unsafe provider proxy instance");
    }

    @Override
    public boolean onCreate() {
        mAuthority = (String)RefInvoker.getField(this, this.getClass(), "mAuthority");
        Log.d("ProviderUnsafeProxy", "onCreate called " + mAuthority);
        return false;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        mProviderInfo = info;
        LogUtil.d("attachInfo", info.authority, info.name);
        super.attachInfo(context, info);
    }

    private Uri buildUri(Uri uri) {
        if (getClass().isMemberClass()) {
            Uri target = Uri.parse(uri.toString().replace(uri.getHost(), PREVIOUS + uri.getHost()));
            return target;
        } else {
            return uri;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.query(buildUri(uri), strings, s, strings1, s1);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.query(buildUri(uri), projection, queryArgs, cancellationSignal);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.query(buildUri(uri), projection, selection, selectionArgs, sortOrder, cancellationSignal);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public String getType(Uri uri) {
        LogUtil.d("getType", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.getType(buildUri(uri));
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        LogUtil.d("insert", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.insert(buildUri(uri), contentValues);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        LogUtil.d("delete", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.delete(buildUri(uri), s, strings);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        LogUtil.d("update", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.update(buildUri(uri), contentValues, s, strings);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        LogUtil.d("openFile", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginShadowProviderClient.openFile(buildUri(uri), mode);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Uri uri = buildUri(Uri.parse("content://" + mProviderInfo.authority));
        long tokoen = Binder.clearCallingIdentity();
        try {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putParcelable(TARGET_URL, uri);
            return PluginShadowProviderClient.call(uri, method, arg, extras);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    public static class ProviderClientUnsafeProxy0 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy1 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy2 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy3 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy4 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy5 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy6 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy7 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy8 extends ProviderClientUnsafeProxy {};
    public static class ProviderClientUnsafeProxy9 extends ProviderClientUnsafeProxy {};
}
