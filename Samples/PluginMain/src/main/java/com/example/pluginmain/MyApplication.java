package com.example.pluginmain;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.DemoApplication;
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
}
