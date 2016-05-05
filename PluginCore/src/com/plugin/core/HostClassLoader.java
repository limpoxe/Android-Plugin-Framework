package com.plugin.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

import com.plugin.content.PluginProviderInfo;
import com.plugin.util.LogUtil;

import dalvik.system.DexClassLoader;

/**
 * 为了支持Receiver和ContentProvider，增加此类。
 * 
 * @author Administrator
 * 
 */
public class HostClassLoader extends DexClassLoader {

	public HostClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	@Override
	public String findLibrary(String name) {
		LogUtil.d("findLibrary", name);
		return super.findLibrary(name);
	}

	@Override
	protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

		//Just for Receiver and service
		if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_RECEIVER)
				|| className.startsWith(PluginIntentResolver.CLASS_PREFIX_SERVICE)) {
			String realName = className.replace(PluginIntentResolver.CLASS_PREFIX_RECEIVER, "").replace(PluginIntentResolver.CLASS_PREFIX_SERVICE, "");
			LogUtil.d("className ", className, "target", realName);
			Class clazz = PluginLoader.loadPluginClassByName(realName);
			if (clazz != null) {
				return clazz;
			} else {
				LogUtil.e("到了这里说明出bug了,这里做个容错处理, 避免出现classnotfound");
				//如果到了这里，说明出bug了,这里做个容错处理
				if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_RECEIVER)) {
					return new BroadcastReceiver(){@Override public void onReceive(Context context, Intent intent) {}}.getClass();
				} else {
					return new Service() {@Override public IBinder onBind(Intent intent) {return null;}}.getClass();
				}
			}
		} else if (className.startsWith(PluginProviderInfo.CLASS_PREFIX)) {
			//Just for contentprovider
			String realName = className.replace(PluginProviderInfo.CLASS_PREFIX, "");
			LogUtil.d("className ", className, "target", realName);
			Class clazz = PluginLoader.loadPluginClassByName(realName);
			if (clazz != null) {
				return clazz;
			} else {
				LogUtil.e("到了这里说明出bug了,这里做个容错处理, 避免出现classnotfound");
				//如果到了这里，说明出bug了, 这里做个容错处理
				return new ContentProvider(){
					@Override
					public boolean onCreate() {return false;}
					@Override
					public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {return null;}
					@Override
					public String getType(Uri uri) {return null;}
					@Override
					public Uri insert(Uri uri, ContentValues values) {return null;}
					@Override
					public int delete(Uri uri, String selection, String[] selectionArgs) {return 0;}
					@Override
					public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {return 0;}
				}.getClass();
			}
		}

		return super.loadClass(className, resolve);
	}

}
