package com.plugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class RefInvoker {

	@SuppressWarnings("rawtypes")
	public static Object invokeStaticMethod(String className, String methodName, Class[] paramTypes,
			Object[] paramValues) {

		return invokeMethod(null, className, methodName, paramTypes, paramValues);

	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object target, String className, String methodName, Class[] paramTypes,
			Object[] paramValues) {

		try {
			Class clazz = Class.forName(className);
			Method method = clazz.getDeclaredMethod(methodName, paramTypes);
			method.setAccessible(true);
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
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	@SuppressWarnings("rawtypes")
	public static Object getFieldObject(Object target, String className, String fieldName) {
		try {
			Class clazz = Class.forName(className);
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Object getStaticFieldObject(String className, String fieldName) {

		return getFieldObject(null, className, fieldName);
	}

	@SuppressWarnings("rawtypes")
	public static void setFieldObject(Object target, String className, String fieldName, Object fieldValue) {
		try {
			Class clazz = Class.forName(className);
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, fieldValue);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void setStaticOjbect(String className, String fieldName, Object fieldValue) {
		setFieldObject(null, className, fieldName, fieldValue);
	}

}