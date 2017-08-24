package com.limpoxe.fairy.manager.mapping;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.limpoxe.fairy.core.FairyGlobal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StubExact {

    private static boolean isPoolInited = false;
	private static Set<String> mExcatStubSet;

	private static String buildExactAction() {
		return FairyGlobal.getHostApplication().getPackageName() + ".STUB_EXACT";
	}

	private static void initStubPool() {
        if (isPoolInited) {
			return;
		}
		loadStubExactly();
		isPoolInited = true;
	}

	private static void loadStubExactly() {
		Intent exactStub = new Intent();
		exactStub.setAction(buildExactAction());
		exactStub.setPackage(FairyGlobal.getHostApplication().getPackageName());

		//精确匹配的activity
		List<ResolveInfo> resolveInfos = FairyGlobal.getHostApplication().getPackageManager().queryIntentActivities(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfos != null && resolveInfos.size() > 0) {
			if (mExcatStubSet == null) {
				mExcatStubSet = new HashSet<String>();
			}
			for(ResolveInfo info:resolveInfos) {
				mExcatStubSet.add(info.activityInfo.name);
			}
		}

		//精确匹配的service
		resolveInfos = FairyGlobal.getHostApplication().getPackageManager().queryIntentServices(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfos != null && resolveInfos.size() > 0) {
			if (mExcatStubSet == null) {
				mExcatStubSet = new HashSet<String>();
			}
			for(ResolveInfo info:resolveInfos) {
				mExcatStubSet.add(info.serviceInfo.name);
			}
		}

        //精确匹配的receiver
        resolveInfos = FairyGlobal.getHostApplication().getPackageManager().queryBroadcastReceivers(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            if (mExcatStubSet == null) {
                mExcatStubSet = new HashSet<String>();
            }
            for(ResolveInfo info:resolveInfos) {
                mExcatStubSet.add(info.activityInfo.name);
            }
        }
    }

	public static boolean isExact(String name, int type) {
        initStubPool();
		if (mExcatStubSet != null && mExcatStubSet.size() > 0) {
			return mExcatStubSet.contains(name);
		}
		return false;
	}
}
