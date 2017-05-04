package com.limpoxe.fairy.core.compat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.SparseArray;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackAssetManager;
import com.limpoxe.fairy.core.android.HackWebViewFactory;
import com.limpoxe.fairy.util.LogUtil;

/**
 * Created by cailiming on 16/10/23.
 * not used
 */

public class CompatForWebViewFactoryApi21 {

    public static void addWebViewAssets(AssetManager assetsManager) {
        if (!FairyGlobal.isLocalHtmlEnable()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            PackageInfo packageInfo = HackWebViewFactory.getLoadedPackageInfo();
            if (packageInfo != null) {
                HackAssetManager hackAssetManager = new HackAssetManager(assetsManager);
                SparseArray<String> packageIdentifiers = hackAssetManager.getAssignedPackageIdentifiers();
                if (!isAdded(packageIdentifiers, packageInfo.packageName)) {
                    LogUtil.i("Loaded WebView Package : " + packageInfo.packageName + " version " + packageInfo.versionName + " (code " + packageInfo.versionCode + ")" + packageInfo.applicationInfo.sourceDir);
                    LogUtil.i(packageInfo.applicationInfo.logo + " " + packageInfo.applicationInfo.icon + " " + packageInfo.applicationInfo.labelRes);
                    hackAssetManager.addAssetPath(packageInfo.applicationInfo.sourceDir);
                }
            }
        }
    }

    public static String getChromeApkPath() {
        if (!FairyGlobal.isLocalHtmlEnable()) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Resources hostRes = FairyGlobal.getApplication().getResources();
                int packageNameResId = hostRes.getIdentifier("android:string/config_webViewPackageName", "string", "android");
                String chromePackagename = hostRes.getString(packageNameResId);
                LogUtil.v("chromePackagename", chromePackagename);
                ApplicationInfo applicationInfo = FairyGlobal.getApplication().createPackageContext(chromePackagename, 0).getApplicationInfo();
                String chromePath = applicationInfo.sourceDir;
                LogUtil.i(applicationInfo.logo + " " + applicationInfo.icon + " " + applicationInfo.labelRes);
                LogUtil.v("chrome app path", chromePath);
                return chromePath;
            } catch (Exception e) {
                //ignore
            }
        }
        return null;
    }

    private static boolean isAdded(SparseArray<String> packageIdentifiers, String packageName) {
        if (packageIdentifiers != null) {
            for (int i = 0; i < packageIdentifiers.size(); i++) {
                final String name = packageIdentifiers.valueAt(i);
                if (packageName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
