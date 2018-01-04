package com.limpoxe.fairy.manager;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.Log;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.manager.mapping.PluginStubBinding;
import com.limpoxe.fairy.manager.mapping.StubExact;
import com.limpoxe.fairy.manager.mapping.StubMappingProcessor;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import static com.limpoxe.fairy.core.bridge.ProviderClientProxy.CALL_PROXY_KEY;
import static com.limpoxe.fairy.core.bridge.ProviderClientProxy.TARGET_URL;

/**
 * Created by cailiming on 16/3/11.
 *
 * 利用ContentProvider实现同步跨进程调用
 *
 */
public class PluginManagerProvider extends ContentProvider {

    private static Uri CONTENT_URI;

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

    public static final String ACTION_BIND_ACTIVITY = "bind_activity";
    public static final String BIND_ACTIVITY_RESULT = "bind_activity_result";

    public static final String ACTION_UNBIND_ACTIVITY = "unbind_activity";
    public static final String UNBIND_ACTIVITY_RESULT = "unbind_activity_result";

    public static final String ACTION_BIND_SERVICE = "bind_service";
    public static final String BIND_SERVICE_RESULT = "bind_service_result";

    public static final String ACTION_GET_BINDED_SERVICE = "get_binded_service";
    public static final String GET_BINDED_SERVICE_RESULT = "get_binded_service_result";

    public static final String ACTION_UNBIND_SERVICE = "unbind_service";
    public static final String UNBIND_SERVICE_RESULT = "unbind_service_result";

    public static final String ACTION_BIND_RECEIVER = "bind_receiver";
    public static final String BIND_RECEIVER_RESULT = "bind_receiver_result";

    public static final String ACTION_IS_EXACT = "is_exact";
    public static final String IS_EXACT_RESULT = "is_exact_result";

    public static final String ACTION_IS_STUB = "is_stub";
    public static final String IS_STUB_RESULT = "is_stub_result";

    public static final String ACTION_IS_PLUGIN_RUNNING = "is_plugin_running";
    public static final String IS_PLUGIN_RUNNING_RESULT = "is_plugin_running_result";

    public static final String ACTION_WAKEUP_PLUGIN = "wakeup_plugin";
    public static final String WAKEUP_PLUGIN_RESULT = "wakeup_plugin_result";

    public static final String ACTION_DUMP_SERVICE_INFO = "dump_service_info";
    public static final String DUMP_SERVICE_INFO_RESULT = "dump_service_info_result";

    public static final String ACTION_REBOOT_PLUGIN_PROCESS = "reboot_plugin_process";


    private PluginManagerService managerService;
    private PluginStatusChangeListener changeListener;
    private Handler mainHandler;

    public static Uri buildUri() {
        if (CONTENT_URI == null) {
            CONTENT_URI = Uri.parse("content://"+ FairyGlobal.getHostApplication().getPackageName() + ".manager" + "/call");
        }
        return CONTENT_URI;
    }

    @Override
    public boolean onCreate() {

        Log.d("PluginManagerProvider", "onCreate, Thread id " + Thread.currentThread().getId() + " name " + Thread.currentThread().getName());

        mainHandler = new Handler(Looper.getMainLooper());
        managerService = new PluginManagerService();
        changeListener = new PluginCallbackImpl();
        managerService.loadInstalledPlugins();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().query(targetUrl, projection, queryArgs, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().getType(targetUrl);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().insert(targetUrl, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().delete(targetUrl, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().update(targetUrl, values, selection, selectionArgs);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Uri targetUrl = Uri.parse(uri.getQueryParameter(TARGET_URL));
        return getContext().getContentResolver().openFileDescriptor(targetUrl, mode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bundle call(String method, String arg, Bundle extras) {

        if (Build.VERSION.SDK_INT >= 19) {
            LogUtil.v("callingPackage = ", getCallingPackage());
        }

        LogUtil.d("跨进程调用统计",
                "Thread id", Thread.currentThread().getId(),
                "name", Thread.currentThread().getName(),
                "method", method,
                "arg", arg);

        if (extras != null && extras.getParcelable(CALL_PROXY_KEY) != null) {
            Uri targetUrl = extras.getParcelable(CALL_PROXY_KEY);
            return getContext().getContentResolver().call(targetUrl, method, arg, extras);
        }

        return dispathToManager(method, arg, extras);
    }

    /**
     * 在跨进程的调用的情况下，provider的方法在binder的线程中被调用，
     * 这个方法可能存在多线程问题
     * 但是直接在dispathToManager这个方法上加同步可能存在死锁风险
     * 因此在managerService等具体的非查询方法上都加了同步处理
     */
    private Bundle dispathToManager(String method, String arg, Bundle extras) {

        Bundle bundle = new Bundle();

        if (ACTION_INSTALL.equals(method)) {

            InstallResult result = managerService.installPlugin(arg);
            bundle.putInt(INSTALL_RESULT, result.getResult());

            changeListener.onInstall(result.getResult(), result.getPackageName(), result.getVersion(), arg);

            return bundle;

        } else if (ACTION_REMOVE.equals(method)) {

            int code = managerService.remove(arg);
            bundle.putInt(REMOVE_RESULT, code);

            changeListener.onRemove(arg, code);

            return bundle;

        } else if (ACTION_REMOVE_ALL.equals(method)) {

            boolean success = managerService.removeAll();
            bundle.putBoolean(REMOVE_ALL_RESULT, success);

            return bundle;

        } else if (ACTION_QUERY_BY_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByPluginId(arg);
            bundle.putSerializable(QUERY_BY_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_CLASS_NAME.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByClassName(arg);
            bundle.putSerializable(QUERY_BY_CLASS_NAME_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_FRAGMENT_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByFragmenetId(arg);
            bundle.putSerializable(QUERY_BY_FRAGMENT_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_ALL.equals(method)) {

            Collection<PluginDescriptor> pluginDescriptorList = managerService.getPlugins();
            ArrayList<PluginDescriptor> result =  new ArrayList<PluginDescriptor>(pluginDescriptorList.size());
            result.addAll(pluginDescriptorList);
            bundle.putSerializable(QUERY_ALL_RESULT, result);

            return bundle;

        } else if (ACTION_BIND_ACTIVITY.equals(method)) {

            bundle.putString(BIND_ACTIVITY_RESULT, PluginStubBinding.bindStub(arg, extras.getString("packageName"), StubMappingProcessor.TYPE_ACTIVITY));

            return bundle;

        } else if (ACTION_UNBIND_ACTIVITY.equals(method)) {

            PluginStubBinding.unBind(arg, extras.getString("className"), StubMappingProcessor.TYPE_ACTIVITY);

        } else if (ACTION_BIND_SERVICE.equals(method)) {
            bundle.putString(BIND_SERVICE_RESULT, PluginStubBinding.bindStub(arg, null, StubMappingProcessor.TYPE_SERVICE));

            return bundle;

        } else if (ACTION_GET_BINDED_SERVICE.equals(method)) {
            bundle.putString(GET_BINDED_SERVICE_RESULT, PluginStubBinding.getBindedPluginClassName(arg, StubMappingProcessor.TYPE_SERVICE));

            return bundle;

        } else if (ACTION_UNBIND_SERVICE.equals(method)) {

            PluginStubBinding.unBind(null, arg, StubMappingProcessor.TYPE_SERVICE);

        } else if (ACTION_BIND_RECEIVER.equals(method)) {
            bundle.putString(BIND_RECEIVER_RESULT, PluginStubBinding.bindStub(arg, null, StubMappingProcessor.TYPE_RECEIVER));

            return bundle;

        } else if (ACTION_IS_EXACT.equals(method)) {
            bundle.putBoolean(IS_EXACT_RESULT, StubExact.isExact(arg, extras.getInt("type")));

            return bundle;

        } else if (ACTION_IS_STUB.equals(method)) {
            bundle.putBoolean(IS_STUB_RESULT, PluginStubBinding.isStub(arg));

            return bundle;

        } else if (ACTION_DUMP_SERVICE_INFO.equals(method)) {
            bundle.putString(DUMP_SERVICE_INFO_RESULT, "TODO: not implement yet");

            return bundle;
        } else if (ACTION_IS_PLUGIN_RUNNING.equals(method)) {
            bundle.putBoolean(IS_PLUGIN_RUNNING_RESULT, PluginLauncher.instance().isRunning(arg));

            return bundle;
        } else if (ACTION_WAKEUP_PLUGIN.equals(method)) {
            LoadedPlugin loadedPlugin = PluginLauncher.instance().startPlugin(arg);
            bundle.putBoolean(WAKEUP_PLUGIN_RESULT, loadedPlugin!=null);

            return bundle;
        } else if (ACTION_REBOOT_PLUGIN_PROCESS.equals(method)) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    //杀进程不能在binder线程执行，否则会导致调用方和被调用方都被杀掉
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                }
            });
            return null;
        }

        return null;
    }

}
