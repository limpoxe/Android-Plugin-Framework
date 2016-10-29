package com.limpoxe.fairy.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RefInvoker {

	public static Object invokeMethod(String className, String methodName, Class[] paramTypes,
									  Object[] paramValues) {
		return invokeMethod(null, className, methodName, paramTypes, paramValues);
	}

	public static Object invokeMethod(Object target, String className, String methodName, Class[] paramTypes,
			Object[] paramValues) {

		try {
			Class clazz = Class.forName(className);
			return invokeMethod(target, clazz, methodName, paramTypes, paramValues);
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object invokeMethod(Object target, Class clazz, String methodName, Class[] paramTypes,
									  Object[] paramValues) {
		try {
			Method method = clazz.getDeclaredMethod(methodName, paramTypes);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			return method.invoke(target, paramValues);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getField(String className, String fieldName) {
		return getField(null, className, fieldName);
	}

	@SuppressWarnings("rawtypes")
	public static Object getField(Object target, String className, String fieldName) {
		try {
			Class clazz = Class.forName(className);
			return getField(target, clazz, fieldName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Object getField(Object target, Class clazz, String fieldName) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return field.get(target);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// try supper for Miui, Miui has a class named MiuiPhoneWindow
			try {
				Field field = clazz.getSuperclass().getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(target);
			} catch (Exception superE) {
				e.printStackTrace();
				superE.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void setField(String className, String fieldName, Object fieldValue) {
		setField(null, className, fieldName, fieldValue);
	}

	@SuppressWarnings("rawtypes")
	public static void setField(Object target, String className, String fieldName, Object fieldValue) {
		try {
			Class clazz = Class.forName(className);
			setField(target, clazz, fieldName, fieldValue);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void setField(Object target, Class clazz, String fieldName, Object fieldValue) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(target, fieldValue);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// try supper for Miui, Miui has a class named MiuiPhoneWindow
			try {
				Field field = clazz.getSuperclass().getDeclaredField(fieldName);
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				field.set(target, fieldValue);
			} catch (Exception superE) {
				e.printStackTrace();
				//superE.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Method findMethod(Object object, String methodName, Class[] clazzes) {
		try {
			return object.getClass().getDeclaredMethod(methodName, clazzes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Method findMethod(Object object, String methodName, Object[] args) {
		if (args == null) {
			try {
				return object.getClass().getDeclaredMethod(methodName, (Class[])null);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			Method[] methods = object.getClass().getDeclaredMethods();
			boolean isFound = false;
			Method method = null;
			for(Method m: methods) {
				if (m.getName().equals(methodName)) {
					Class<?>[] types = m.getParameterTypes();
					if (types.length == args.length) {
						isFound = true;
						for(int i = 0; i < args.length; i++) {
							if (!(types[i] == args[i].getClass() || (types[i].isPrimitive() && primitiveToWrapper(types[i]) == args[i].getClass()))) {
								isFound = false;
								break;
							}
						}
						if (isFound) {
							method = m;
							break;
						}
					}
				}
			}
			return  method;
		}
	}

	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();

	static {
		primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		primitiveWrapperMap.put(Character.TYPE, Character.class);
		primitiveWrapperMap.put(Short.TYPE, Short.class);
		primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		primitiveWrapperMap.put(Long.TYPE, Long.class);
		primitiveWrapperMap.put(Double.TYPE, Double.class);
		primitiveWrapperMap.put(Float.TYPE, Float.class);
		primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
	}

	static Class<?> primitiveToWrapper(final Class<?> cls) {
		Class<?> convertedClass = cls;
		if (cls != null && cls.isPrimitive()) {
			convertedClass = primitiveWrapperMap.get(cls);
		}
		return convertedClass;
	}

	public static <T> Wrapper wrap(T instance) {
		return new Wrapper(instance);
	}

	public static class Wrapper {

		Object instance;

		Wrapper(Object instance) {
			this.instance = instance;
		}

		public <K> K with(Class<K> k) {
			try {
				return k.getConstructor(Object.class).newInstance(instance);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}