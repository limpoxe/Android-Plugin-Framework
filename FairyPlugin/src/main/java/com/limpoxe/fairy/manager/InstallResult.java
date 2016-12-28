package com.limpoxe.fairy.manager;

/**
 * Created by cailiming on 16/6/20.
 */
public class InstallResult {

    private int mResult;
    private String mPackageName;
    private String mVersion;

    public InstallResult(int result) {
        this.mResult = result;
    }

    public InstallResult(int result, String packageName, String version) {
        this.mResult = result;
        this.mPackageName = packageName;
        this.mVersion = version;
    }

    public int getResult() {
        return mResult;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getVersion() {
        return mVersion;
    }
}
