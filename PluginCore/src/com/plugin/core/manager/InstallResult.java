package com.plugin.core.manager;

/**
 * Created by cailiming on 16/6/20.
 */
public class InstallResult {

    public static final int SUCCESS = 0;
    public static final int SRC_FILE_NOT_FOUND = 1;
    public static final int COPY_FILE_FAIL = 2;
    public static final int SIGNATURES_INVALIDATE = 3;
    public static final int VERIFY_SIGNATURES_FAIL = 4;
    public static final int PARSE_MANIFEST_FAIL = 5;
    public static final int FAIL_BECAUSE_HAS_LOADED = 6;
    public static final int INSTALL_FAIL = 7;

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
