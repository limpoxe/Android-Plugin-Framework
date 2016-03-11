package com.plugin.core.manager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by cailiming on 16/3/11.
 *
 * 利用ContentProvider实现同步跨进程调用
 *
 */
public class PluginManagerProvider extends ContentProvider {

    public static final String AUTHORITY = "com.plugin.core.manager.PluginManagerProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/call");

    public static final String ACTION_INSTALL = "install";
    public static final String INSTALL_RESULT = "install_result";

    public static final String ACTION_REMOVE = "remove";
    public static final String REMOVE_RESULT = "remove_result";

    public static final String ACTION_REMOVE_ALL = "remove_all";
    public static final String REMOVE_ALL_RESULT = "remove_all_result";

    public static final String ACTION_QUERY_BY_ID = "query_by_id";
    public static final String QUERY_BY_ID_RESULT = "query_by_id_result";

    public static final String ACTION_QUERY_BY_CLASS_NAME = "query_by_class_name";
    public static final String QUERY_BY_CLASS_NAME_RESULT = "query_by_class_name_result";

    public static final String ACTION_QUERY_BY_FRAGMENT_ID = "query_by_fragment_id";
    public static final String QUERY_BY_FRAGMENT_ID_RESULT = "query_by_fragment_id_result";

    public static final String ACTION_QUERY_ALL = "query_all";
    public static final String QUERY_ALL_RESULT = "query_all_result";

    private Object mLockObject = new Object();

    private PluginManagerImpl manager;

    @Override
    public boolean onCreate() {
        manager = new PluginManagerImpl();
        manager.loadInstalledPlugins();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //doNothing
        return null;
    }

    @Override
    public String getType(Uri uri) {
        //doNothing
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //doNothing
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //doNothing
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //doNothing
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {

        LogUtil.d("Thead : id = " + Thread.currentThread().getId()
                + ", name = " + Thread.currentThread().getName()
                + ", callingPackage = " + getCallingPackage()
                + ", method = " + method
                + ", arg = " + arg);

        synchronized (mLockObject) {
            Bundle bundle = new Bundle();
            if (ACTION_INSTALL.equals(method)) {

                int result = manager.installPlugin(arg);
                bundle.putInt(INSTALL_RESULT, result);

                return bundle;

            } else if (ACTION_REMOVE.equals(method)) {

                boolean success = manager.remove(arg);
                bundle.putBoolean(REMOVE_RESULT, success);

                return bundle;

            } else if (ACTION_REMOVE_ALL.equals(method)) {

                boolean success = manager.removeAll();
                bundle.putBoolean(REMOVE_ALL_RESULT, success);

                return bundle;

            } else if (ACTION_QUERY_BY_ID.equals(method)) {

                PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByPluginId(arg);
                bundle.putSerializable(QUERY_BY_ID_RESULT, pluginDescriptor);

                return bundle;

            } else if (ACTION_QUERY_BY_CLASS_NAME.equals(method)) {

                PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByClassName(arg);
                bundle.putSerializable(QUERY_BY_CLASS_NAME_RESULT, pluginDescriptor);

                return bundle;

            } else if (ACTION_QUERY_BY_FRAGMENT_ID.equals(method)) {

                PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByFragmenetId(arg);
                bundle.putSerializable(QUERY_BY_FRAGMENT_ID_RESULT, pluginDescriptor);

                return bundle;

            } else if (ACTION_QUERY_ALL.equals(method)) {

                Collection<PluginDescriptor> pluginDescriptorList = manager.getPlugins();
                ArrayList<PluginDescriptor> result =  new ArrayList<PluginDescriptor>(pluginDescriptorList.size());
                result.addAll(pluginDescriptorList);
                bundle.putSerializable(QUERY_ALL_RESULT, result);

                return bundle;

            }
        }
        return null;
    }
}
