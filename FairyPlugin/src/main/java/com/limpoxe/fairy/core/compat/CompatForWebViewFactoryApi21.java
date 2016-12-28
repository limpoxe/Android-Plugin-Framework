package com.limpoxe.fairy.core.compat;

import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.SparseArray;

import com.limpoxe.fairy.core.android.HackAssetManager;
import com.limpoxe.fairy.core.android.HackWebViewFactory;
import com.limpoxe.fairy.util.LogUtil;

/**
 * Created by cailiming on 16/10/23.
 * not used
 */

public class CompatForWebViewFactoryApi21 {

    public static void addWebViewAssets(AssetManager assetsManager) {
        if (Build.VERSION.SDK_INT >= 21) {
            PackageInfo packageInfo = HackWebViewFactory.getLoadedPackageInfo();
            if (packageInfo != null) {
                HackAssetManager hackAssetManager = new HackAssetManager(assetsManager);
                SparseArray<String> packageIdentifiers = hackAssetManager.getAssignedPackageIdentifiers();
                if (!isAdded(packageIdentifiers, packageInfo.packageName)) {
                    LogUtil.i("Loaded WebView Package : " + packageInfo.packageName + " version " + packageInfo.versionName + " (code " + packageInfo.versionCode + ")" + packageInfo.applicationInfo.sourceDir);
                    hackAssetManager.addAssetPath(packageInfo.applicationInfo.sourceDir);
                }
            }
        }
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
