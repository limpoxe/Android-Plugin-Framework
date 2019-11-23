package com.example.pluginmain;

public class CxxTest {

    public static native String stringFromJNI();

    public static native int println(int bufferId, int level, String tag, String msg);

    static {
        System.loadLibrary("cxxTest");
    }

}
