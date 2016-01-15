package com.plugin.util;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class RefProxy {

	public static Object asInterface(Object target) {
		Class<?> clazz = target.getClass();
		List<Class<?>> interfaces = getAllInterfaces(clazz);
		Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), ifs, new MethodHandler(target));
	}

	private static List<Class<?>> getAllInterfaces(final Class<?> cls) {
		if (cls == null) {
			return null;
		}
		final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
		getAllInterfaces(cls, interfacesFound);
		return new ArrayList<Class<?>>(interfacesFound);
	}

	private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
		while (cls != null) {
			final Class<?>[] interfaces = cls.getInterfaces();

			for (final Class<?> i : interfaces) {
				if (interfacesFound.add(i)) {
					getAllInterfaces(i, interfacesFound);
				}
			}

			cls = cls.getSuperclass();
		}
	}

}
