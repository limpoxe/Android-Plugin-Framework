package com.limpoxe.fairy.core.bridge;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.limpoxe.fairy.manager.PluginManagerProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.FileNotFoundException;

/**
 * Created by cailiming on 2017/11/27.
 */
public class ProviderClientProxy extends ContentProvider {

    public static final String CALL_PROXY_KEY = "target_call";
    public static final String TARGET_URL = "targetUrl";

    private String mAuthority = null;

    public ProviderClientProxy() {
       Log.d("ProviderClientProxy", "create provider proxy instance");
    }

    @Override
    public boolean onCreate() {
        mAuthority = (String)RefInvoker.getField(this, this.getClass(), "mAuthority");
        Log.d("ProviderClientProxy", "onCreate called " + mAuthority);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        LogUtil.d("query", uri);
        return PluginManagerProviderClient.query(uri, strings, s, strings1, s1);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        return PluginManagerProviderClient.query(uri, projection, queryArgs, cancellationSignal);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        LogUtil.d("query", uri);
        return PluginManagerProviderClient.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        LogUtil.d("getType", uri);
        return PluginManagerProviderClient.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        LogUtil.d("insert", uri);
        return PluginManagerProviderClient.insert(uri, contentValues);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        LogUtil.d("delete", uri);
        return PluginManagerProviderClient.delete(uri, s, strings);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        LogUtil.d("update", uri);
        return PluginManagerProviderClient.update(uri, contentValues, s, strings);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        //约定：原始url被吞掉了，所以调用这个函数的时候需要同时将原始url放入extras
        LogUtil.d(CALL_PROXY_KEY);
        if (extras != null && extras.getParcelable(CALL_PROXY_KEY) != null) {
            return PluginManagerProviderClient.call(method, arg, extras);
        }
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        LogUtil.d("openFile", uri);
        return PluginManagerProviderClient.openFile(uri, mode);
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        LogUtil.d("attachInfo", info.authority, info.name);
        super.attachInfo(context, info);
    }

}
