package com.limpoxe.fairy.core.compat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
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

    public static void addWebViewAssets(AssetManager pluginAssetManager) {
        if (!FairyGlobal.isLocalHtmlEnable()) {
            return;
        }
        PackageInfo packageInfo = HackWebViewFactory.getLoadedPackageInfo();
        if (packageInfo != null) {
            HackAssetManager hackAssetManager = new HackAssetManager(pluginAssetManager);
            SparseArray<String> packageIdentifiers = null;
            //Android L 及以上AssetManager才有getAssignedPackageIdentifiers这个函数
            if (Build.VERSION.SDK_INT >= 21) {
                packageIdentifiers = hackAssetManager.getAssignedPackageIdentifiers();
                //Beign:Just For Debug
                HackAssetManager hackhostAssetManager = new HackAssetManager(FairyGlobal.getApplication().getAssets());
                SparseArray<String> hostPackageIdentifiers = hackhostAssetManager.getAssignedPackageIdentifiers();
                printPackages(hostPackageIdentifiers);
                LogUtil.v("------");
                printPackages(packageIdentifiers);
                //End:Just For Debug
            }
            //如果插件的AssetManager尚未添加webview的包，则补上。
            if (!isAdded(packageIdentifiers, packageInfo.packageName)) {
                LogUtil.i("Loaded WebView Package : " + packageInfo.packageName + " version " + packageInfo.versionName + " (code " + packageInfo.versionCode + ")" + packageInfo.applicationInfo.sourceDir);
                LogUtil.i("WebView logo " + packageInfo.applicationInfo.logo + "，icon " + packageInfo.applicationInfo.icon + ", labelRes" + packageInfo.applicationInfo.labelRes);
                hackAssetManager.addAssetPath(packageInfo.applicationInfo.sourceDir);
            }
        } else {
            String chrome = getChromeApkPath();
            LogUtil.v("getChromeApkPath", chrome);
            if (!TextUtils.isEmpty(chrome)) {
                HackAssetManager hackAssetManager = new HackAssetManager(pluginAssetManager);
                hackAssetManager.addAssetPath(chrome);
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

    private static void printPackages(SparseArray<String> packageIdentifiers) {
        if (packageIdentifiers != null) {
            for (int i = 0; i < packageIdentifiers.size(); i++) {
                LogUtil.v("packageIdentifiers", i, packageIdentifiers.valueAt(i));
            }
        }
    }
}
