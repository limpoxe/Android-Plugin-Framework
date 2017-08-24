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
                HackAssetManager hackhostAssetManager = new HackAssetManager(FairyGlobal.getHostApplication().getAssets());
                SparseArray<String> hostPackageIdentifiers = hackhostAssetManager.getAssignedPackageIdentifiers();
                printPackages(hostPackageIdentifiers);
                LogUtil.v("------------------------------------");
                printPackages(packageIdentifiers);
                //End:Just For Debug
            }
            //如果插件的AssetManager尚未添加webview的包，则补上。
            if (!isAdded(packageIdentifiers, packageInfo.packageName)) {
                LogUtil.i("Loaded WebView Package : " + packageInfo.packageName + " version " + packageInfo.versionName + " (code " + packageInfo.versionCode + ")" + packageInfo.applicationInfo.sourceDir);
                LogUtil.i("WebView logo " + packageInfo.applicationInfo.logo + "，icon " + packageInfo.applicationInfo.icon + ", labelRes " + packageInfo.applicationInfo.labelRes);
                //TODO 由于目前的资源id分组方案是限制宿主范围，插件使用原生范围，因此如果webview也使用了原生的范围，则会和插件冲突
                //为避免webview的资源id和插件的资源id冲突，这里做个判断
                if (packageInfo.applicationInfo.icon != 0 && (packageInfo.applicationInfo.icon >> 24) != 0x7f) {
                    //Android System WebView
                    LogUtil.w("add webview assets " + packageInfo.applicationInfo.sourceDir);
                    hackAssetManager.addAssetPath(packageInfo.applicationInfo.sourceDir);
                } else {
                    //Chrome
                    LogUtil.w("WebView Assets Not Added " + packageInfo.applicationInfo.sourceDir);
                    //TODO 既然宿主可以使用和自己相同packageId的webview，可能问题还是出在assets的添加顺序上。
                }
            }
        } else {
            ApplicationInfo chrome = getWebViewPackage();
            if (chrome != null) {
                String chromePath = chrome.sourceDir;
                LogUtil.i("WebView logo " + chrome.logo + "，icon " + chrome.icon + ", labelRes" + chrome.labelRes + ", path " + chromePath);
                if (chrome.icon != 0 && (chrome.icon >> 24) != 0x7f) {
                    //Android System WebView
                    LogUtil.w("add webview assets " + chromePath);
                    HackAssetManager hackAssetManager = new HackAssetManager(pluginAssetManager);
                    hackAssetManager.addAssetPath(chromePath);
                } else {
                    //Chrome
                    LogUtil.w("WebView Assets Not Added " + chromePath);
                    //TODO 既然宿主可以使用和自己相同packageId的webview，可能问题还是出在assets的添加顺序上。
                }
            }
        }
    }

    public static ApplicationInfo getWebViewPackage() {
        if (!FairyGlobal.isLocalHtmlEnable()) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Resources hostRes = FairyGlobal.getHostApplication().getResources();
                int packageNameResId = hostRes.getIdentifier("android:string/config_webViewPackageName", "string", "android");
                String chromePackagename = hostRes.getString(packageNameResId);
                LogUtil.v("Webview PackageName", chromePackagename);
                ApplicationInfo applicationInfo = FairyGlobal.getHostApplication().createPackageContext(chromePackagename, 0).getApplicationInfo();
                return applicationInfo;
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
