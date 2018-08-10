package com.limpoxe.fairy.core.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;

import com.limpoxe.fairy.util.LogUtil;

/**
 * 这个wrapper目前并不需要，这里先留着，或许后面会有其他用处
 */
@TargetApi(28)
public class CompatForAppComponentFactoryApi28 extends AppComponentFactory {
    private static final String TAG = "CompatForAppComponentFactoryApi28";
    private AppComponentFactory real;

    public CompatForAppComponentFactoryApi28(AppComponentFactory src) {
        super();
        this.real = src;
    }

    @Override
    public Application instantiateApplication(ClassLoader cl, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //这个函数应该不会进来。框架初始化时已经错过了Application的初始化时机
        //除非将CompatForAppComponentFactoryApi28注册到宿主的Manitest中
        LogUtil.d(TAG, "instantiateApplication");
        return real.instantiateApplication(cl, className);
    }

    @Override
    public Activity instantiateActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        LogUtil.d(TAG, "instantiateActivity");
        return real.instantiateActivity(cl, className, intent);
    }

    @Override
    public BroadcastReceiver instantiateReceiver(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        LogUtil.d(TAG, "instantiateReceiver");
        return real.instantiateReceiver(cl, className, intent);
    }

    @Override
    public Service instantiateService(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        LogUtil.d(TAG, "instantiateService");
        return real.instantiateService(cl, className, intent);
    }

    @Override
    public ContentProvider instantiateProvider(ClassLoader cl, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //这个函数不一定会进来，受provier和application的初始化顺序影响
        //除非将CompatForAppComponentFactoryApi28注册到宿主的Manitest中
        LogUtil.d(TAG, "instantiateProvider");
        return real.instantiateProvider(cl, className);
    }
}
