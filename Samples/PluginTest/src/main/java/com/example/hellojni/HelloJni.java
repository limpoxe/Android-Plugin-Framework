package com.example.hellojni;

public class HelloJni {

	public static native int calculate(int digit_1, int digit_2);

	static {
		System.loadLibrary("hello-jni");
	}
}
