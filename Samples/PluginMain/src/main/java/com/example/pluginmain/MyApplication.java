package com.example.pluginmain;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginApplication;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends PluginApplication {
	@Override
	public void onCreate() {
		super.onCreate();

        //bugly SDK
        CrashReport.initCrashReport(getApplicationContext(), "c38ae3f8a6", true);

        FairyGlobal.setLogEnable(true);
        FairyGlobal.setLocalHtmlenable(true);
        //可选, 指定loading页UI, 用于首次加载插件时, 显示菊花等待插件加载完毕,
        FairyGlobal.setLoadingResId(R.layout.loading);
	}
}
