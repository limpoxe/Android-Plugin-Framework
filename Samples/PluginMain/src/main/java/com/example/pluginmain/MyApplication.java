package com.example.pluginmain;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import com.limpoxe.fairy.core.DemoApplication;
import com.limpoxe.fairy.core.FairyGlobal;
import com.tencent.bugly.crashreport.CrashReport;
//import com.tencent.bugly.crashreport.CrashReport;
//import com.umeng.analytics.MobclickAgent;

public class MyApplication extends DemoApplication {
	@Override
	public void onCreate() {
		super.onCreate();

        //bugly SDK
        CrashReport.initCrashReport(getApplicationContext(), "c38ae3f8a6", true);

        //UMENG SDK
        //MobclickAgent.setDebugMode(true);
        //MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
	}

    @Override
    protected void attachBaseContext(Context base) {
	    FairyGlobal.setLogEnable(true);
        FairyGlobal.setLocalHtmlenable(false);
        //not mecessary
        FairyGlobal.setLoadingResId(R.layout.loading);
        //Just for test custom Mapping Processor
        FairyGlobal.registStubMappingProcessor(new TestCoustProcessor());

        if (Build.VERSION.SDK_INT >= 28) {
            WebView.setDataDirectorySuffix(getProcessName().replaceAll("[\\.:]", "_"));
        }
        super.attachBaseContext(base);
    }
}
