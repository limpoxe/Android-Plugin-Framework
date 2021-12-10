package com.limpoxe.fairy.core.bridge;

import static com.limpoxe.fairy.core.bridge.ProviderClientUnsafeProxy.TARGET_URL;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.Log;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.manager.PluginManagerProvider;
import com.limpoxe.fairy.util.LogUtil;

import java.io.FileNotFoundException;

public class PluginShadowProvider extends ContentProvider {

    private static Uri CONTENT_URI;

    public static Uri buildUri() {
        if (CONTENT_URI == null) {
            CONTENT_URI = Uri.parse("content://"+ FairyGlobal.getHostApplication().getPackageName() + ".bridge" + "/");
        }
        return CONTENT_URI;
    }

    public PluginShadowProvider() {
        Log.e("PluginShadowProvider", "create instance");
    }

    @Override
    public boolean onCreate() {
        Log.d("PluginShadowProvider", "onCreate, Thread id " + Thread.currentThread().getId() + " name " + Thread.currentThread().getName() + " pid " + Process.myPid());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("query", targetUrl.toString());
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("query", targetUrl.toString());
        return getContext().getContentResolver().query(targetUrl, projection, queryArgs, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("query", targetUrl.toString());
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("getType", targetUrl.toString());
        return getContext().getContentResolver().getType(targetUrl);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("insert", targetUrl.toString());
        return getContext().getContentResolver().insert(targetUrl, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("delete", targetUrl.toString());
        return getContext().getContentResolver().delete(targetUrl, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("update", targetUrl.toString());
        return getContext().getContentResolver().update(targetUrl, values, selection, selectionArgs);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        LogUtil.d("openFile", targetUrl.toString());
        return getContext().getContentResolver().openFileDescriptor(targetUrl, mode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (extras != null && extras.getParcelable(TARGET_URL) != null) {
            Uri targetUrl = extras.getParcelable(TARGET_URL);
            LogUtil.d("call", targetUrl.toString());
            //安全防范
            if (!targetUrl.getAuthority().contains(PluginManagerProvider.buildUri().getAuthority())) {
                return getContext().getContentResolver().call(targetUrl, method, arg, extras);
            }
        }
        return null;
    }

}
