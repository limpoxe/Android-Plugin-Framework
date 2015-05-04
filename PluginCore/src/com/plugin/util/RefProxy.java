package com.plugin.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.util.Log;

public class RefProxy implements InvocationHandler {
	private static final String LOG_TAG = RefProxy.class.getSimpleName();

	private Object target = null;

	public Object bind(Object target) {
		this.target = target;
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Log.v(LOG_TAG, "invoke " + method + " " + Boolean.toString(Proxy.isProxyClass(proxy.getClass())));
		method.setAccessible(true);
		Object result = method.invoke(target, args);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInterface(@SuppressWarnings("rawtypes") Class clazz) {
		RefProxy ph = new RefProxy();
		try {
			T obj = (T) ph.bind(clazz.newInstance());
			return obj;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
