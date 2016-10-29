package com.limpoxe.fairy.core.compat;

import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.SparseArray;

import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.RefInvoker;

/**
 * Created by cailiming on 16/10/23.
 */

public class CompatForWebViewFactoryApi21 {

    public static void addWebViewAssets(AssetManager assetsManager) {
        if (Build.VERSION.SDK_INT >= 21) {
            PackageInfo packageInfo = (PackageInfo) RefInvoker.invokeMethod("android.webkit.WebViewFactory", "getLoadedPackageInfo", new Class[0], new Object[0]);
            if (packageInfo != null) {
                if (!isAdded(assetsManager, packageInfo.packageName)) {
                    LogUtil.i("Loaded WebView Package : " + packageInfo.packageName + " version " + packageInfo.versionName + " (code " + packageInfo.versionCode + ")" + packageInfo.applicationInfo.sourceDir);
                    RefInvoker.invokeMethod(assetsManager, AssetManager.class, "addAssetPath", new Class[]{String.class}, new Object[]{packageInfo.applicationInfo.sourceDir});
                }
            }
        }
    }

    private static boolean isAdded(AssetManager assetsManager, String packageName) {
        SparseArray<String> packageIdentifiers = (SparseArray<String>) RefInvoker.invokeMethod(assetsManager,
                AssetManager.class, "getAssignedPackageIdentifiers", null, null);
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
