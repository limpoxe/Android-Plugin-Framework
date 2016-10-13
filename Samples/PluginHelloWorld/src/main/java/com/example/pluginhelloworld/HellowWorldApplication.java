package com.example.pluginhelloworld;

import android.app.Application;
import android.util.Log;


/**
 * 独立插件测试demo
 */
public class HellowWorldApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("HelloWorld", "Application onCreate");
    }
}
