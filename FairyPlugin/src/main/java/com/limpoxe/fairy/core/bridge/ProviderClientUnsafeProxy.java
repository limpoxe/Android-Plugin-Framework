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

import com.limpoxe.fairy.manager.PluginManagerProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.FileNotFoundException;

import static com.limpoxe.fairy.core.bridge.ProviderClientProxy.CALL_PROXY_KEY;

public class ProviderClientUnsafeProxy extends ContentProvider {

    private static String PREVIOUS = "unsafe.proxy.";

    private ProviderInfo providerInfo;
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

    private static Uri buildTargetUri(Uri uri) {
        Uri target = Uri.parse(uri.toString().replace(uri.getHost(), PREVIOUS + uri.getHost()));
        return target;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.query(buildTargetUri(uri), strings, s, strings1, s1);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.query(buildTargetUri(uri), projection, queryArgs, cancellationSignal);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.query(buildTargetUri(uri), projection, selection, selectionArgs, sortOrder, cancellationSignal);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public String getType(Uri uri) {
        LogUtil.d("getType", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.getType(buildTargetUri(uri));
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        LogUtil.d("insert", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.insert(buildTargetUri(uri), contentValues);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        LogUtil.d("delete", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.delete(buildTargetUri(uri), s, strings);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        LogUtil.d("update", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.update(buildTargetUri(uri), contentValues, s, strings);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        long tokoen = Binder.clearCallingIdentity();
        try {
            if (providerInfo != null) {
                if (extras == null) {
                    extras = new Bundle();
                }
                extras.putParcelable(CALL_PROXY_KEY, buildTargetUri(Uri.parse("content://" + providerInfo.authority)));
                return PluginManagerProviderClient.call(method, arg, extras);
            }
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        LogUtil.d("openFile", uri);
        long tokoen = Binder.clearCallingIdentity();
        try {
            return PluginManagerProviderClient.openFile(buildTargetUri(uri), mode);
        } finally {
            Binder.restoreCallingIdentity(tokoen);
        }
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        providerInfo = info;
        LogUtil.d("attachInfo", info.authority, info.name);
        super.attachInfo(context, info);
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
