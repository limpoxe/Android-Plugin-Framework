package com.plugin.core.proxy;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.plugin.core.PluginIntentResolver;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

/**
 * 由于service的特殊性，采用欺骗的方式加载插件Service时只能同时存在一个实例
 * 
 * 所以这里仍然通过service代理的方式来支持多Service
 * 
 * @author cailiming
 * 
 */
public class PluginProxyService extends Service {

	private final HashMap<String, Service> serviceMap = new HashMap<String, Service>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null) {

			String action = intent.getAction();
			LogUtil.d("onStartCommand action", action);

			if (action != null) {

				final boolean isDestoryCommond = action.contains(PluginIntentResolver.SERVICE_STOP_ACTION_IN_PLUGIN);
				String[] targetClassName;
				if (isDestoryCommond) {
					targetClassName = action.split(PluginIntentResolver.SERVICE_STOP_ACTION_IN_PLUGIN);
				} else {
					targetClassName = action.split(PluginIntentResolver.SERVICE_START_ACTION_IN_PLUGIN);
				}

				String clazzName = targetClassName[0];

				LogUtil.d("tagertServiceClass ", clazzName);

				if (clazzName != null) {

					Service service = serviceMap.get(clazzName);
					Class clazz = null;
					if (service == null) {
						if (isDestoryCommond) {
							return super.onStartCommand(intent, flags, startId);
						} else {
							clazz = PluginLoader.loadPluginClassByName(clazzName);
							intent.setExtrasClassLoader(clazz.getClassLoader());
							try {
								service = (Service) clazz.newInstance();
							} catch (Exception e) {
								e.printStackTrace();
							}
							attach(service);
							service.onCreate();
							serviceMap.put(clazzName, service);
						}
					} else {
						clazz = service.getClass();
						intent.setExtrasClassLoader(clazz.getClassLoader());
					}

					if (isDestoryCommond) {
						service.onDestroy();
						serviceMap.remove(clazzName);
					} else {
						//由于之前intent被修改过 这里再吧Intent还原到原始的intent
						if (targetClassName.length > 1) {
							intent.setAction(targetClassName[1]);
						} else {
							intent.setAction(null);
						}
						service.onStartCommand(intent, flags, startId);
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
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
		if (Build.VERSION.SDK_INT >= 14) {
			Iterator<Service> itr = serviceMap.values().iterator();
			while (itr.hasNext()) {
				try {
					itr.next().onTrimMemory(level);
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	private void set(Service service, String name) {
		LogUtil.d("attach " + name);
		Object obj = RefInvoker.getFieldObject(this, Service.class.getName(), name);
		if (obj != null) {
			RefInvoker.setFieldObject(service, Service.class.getName(), name, obj);
		}
	}

}
