package com.example.pluginmain;

public class CxxTest {

    public static native String stringFromJNI();

    static {
        System.loadLibrary("cxxTest");
    }

}
