package com.plugin.core.proxy;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.IBinder;

import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

/**
 * 由于service的特殊性，采用欺骗的方式加载插件Service时只能同时存在一个实例
 * 
 * 所以这里提供service代理的方式来支持多Service
 * 
 * @author cailiming
 * 
 */
public class PluginProxyService extends Service {

	public static final String SERVICE_NAME = "PluginProxyService.service_name";
	public static final String DESTORY_SERVICE = "PluginProxyService.destory_service";

	private final HashMap<String, Service> serviceMap = new HashMap<String, Service>();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Service service = null;
			String tagertServiceClass = intent.getStringExtra(SERVICE_NAME);
			boolean isDestroy = intent.getBooleanExtra(DESTORY_SERVICE, false);

			LogUtil.d("tagertServiceClass ", tagertServiceClass, intent);
			if (tagertServiceClass != null) {
				service = serviceMap.get(tagertServiceClass);
				if (service == null) {
					if (isDestroy) {
						return super.onStartCommand(intent, flags, startId);
					}
					@SuppressWarnings("rawtypes")
					Class clazz = PluginLoader.loadPluginClassByName(tagertServiceClass);
					try {
						service = (Service) clazz.newInstance();
						attach(service);
						service.onCreate();
						serviceMap.put(tagertServiceClass, service);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (isDestroy) {
					service.onDestroy();
					serviceMap.remove(tagertServiceClass);
				} else {
					service.onStartCommand(intent, flags, startId);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void attach(Service service) {
		RefInvoker.invokeMethod(service, ContextWrapper.class.getName(), "attachBaseContext",
				new Class[] { Context.class },
				new Object[] { PluginLoader.getDefaultPluginContext(service.getClass()) });

		set(service, "mClassName");
		set(service, "mToken");
		set(service, "mApplication");
		set(service, "mActivityManager");
		set(service, "mStartCompatibility");
	}

	/**
	 * 暂不支持bind
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLowMemory() {
		Iterator<Service> itr = serviceMap.values().iterator();
		while (itr.hasNext()) {
			try {
				itr.next().onLowMemory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onTrimMemory(int level) {
		Iterator<Service> itr = serviceMap.values().iterator();
		while (itr.hasNext()) {
			try {
				itr.next().onTrimMemory(level);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		Iterator<Service> itr = serviceMap.values().iterator();
		while (itr.hasNext()) {
			try {
				itr.next().onDestroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		serviceMap.clear();
	}

	private void set(Service service, String name) {
		LogUtil.d("attach " + name);
		Object obj = RefInvoker.getFieldObject(this, Service.class.getName(), name);
		if (obj != null) {
			RefInvoker.setFieldObject(service, Service.class.getName(), name, obj);
		}
	}

}
