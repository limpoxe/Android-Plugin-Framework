package com.limpoxe.fairy.core.bridge;

import static com.limpoxe.fairy.core.bridge.ProviderClientUnsafeProxy.TARGET_URL;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.compat.CompatForContentProvider;
import com.limpoxe.fairy.util.LogUtil;

public class PluginShadowProviderClient {

    private static Uri buildUri(Uri url) {
        return PluginShadowProvider.buildUri().buildUpon().appendQueryParameter(TARGET_URL, url.toString()).build();
    }

    public static Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.query(newUri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            LogUtil.printException("query " + url, e);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.query(newUri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        } catch (Exception e) {
            LogUtil.printException("query " + url, e);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static Cursor query(Uri url, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.query(newUri, projection, queryArgs, cancellationSignal);
        } catch (Exception e) {
            LogUtil.printException("query " + url, e);
        }
        return null;
    }

    public static String getType(Uri url) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.getType(newUri);
        } catch (Exception e) {
            LogUtil.printException("getType " + url, e);
        }
        return null;
    }

    public static Uri insert(Uri url, ContentValues contentValues) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.insert(newUri, contentValues);
        } catch (Exception e) {
            LogUtil.printException("insert " + url, e);
        }
        return null;
    }

    public static int delete(Uri url, String where, String[] selectionArgs) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.delete(newUri, where, selectionArgs);
        } catch (Exception e) {
            LogUtil.printException("delete " + url, e);
        }
        return -1;
    }

    public static int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
        Uri newUri = buildUri(url);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.update(newUri, values, where, selectionArgs);
        } catch (Exception e) {
            LogUtil.printException("update " + url, e);
        }
        return -1;
    }

    public static ParcelFileDescriptor openFile(Uri uri, String mode) {
        Uri newUri = buildUri(uri);
        ContentResolver resolver = FairyGlobal.getHostApplication().getContentResolver();
        try {
            return resolver.openFileDescriptor(newUri, mode);
        } catch (Exception e) {
            LogUtil.printException("openFile " + uri + ", " + newUri, e);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bundle call(Uri url, String method, String arg, Bundle extras) {
        Uri newUri = buildUri(url);
        if(extras == null) {
            extras = new Bundle();
        }
        //newUri里面的TARGET_URL参数在转到provider的call函数后会丢失，所以call函数的url需要放到extras中
        extras.putParcelable(TARGET_URL, url);
        return CompatForContentProvider.call(newUri, method, arg, extras);
    }

}
