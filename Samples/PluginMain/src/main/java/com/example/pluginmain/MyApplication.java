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

        FairyGlobal.setLogEnable(true);
        FairyGlobal.setLocalHtmlenable(false);
        FairyGlobal.setLoadingResId(R.layout.loading);

        //Just for test custom Mapping Processor
        FairyGlobal.registStubMappingProcessor(new TestCoustProcessor());
	}

    @Override
    protected void attachBaseContext(Context base) {
        if (Build.VERSION.SDK_INT >= 28) {
            WebView.setDataDirectorySuffix(getProcessName().replaceAll("[\\.:]", "_"));
        }
        super.attachBaseContext(base);
    }
}
