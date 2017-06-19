package com.limpoxe.fairy.content;

import android.content.pm.ActivityInfo;

import java.io.Serializable;

/**
 * <Pre>
 * @author cailiming
 * </Pre>
 *
 */
public class PluginActivityInfo implements Serializable {

	private String name;//string
	private String windowSoftInputMode;//strin
	private String hardwareAccelerated;//int string
	private String launchMode = String.valueOf(ActivityInfo.LAUNCH_MULTIPLE);//string
	private String screenOrientation;//string
	private String theme;//int
	private String immersive;//int string
	private String uiOptions;
	private int configChanges;
    private boolean useHostPackageName = false;

	public int getConfigChanges() {
		return configChanges;
	}

	public void setConfigChanges(int configChanges) {
		this.configChanges = configChanges;
	}

	public String getUiOptions() {
		return uiOptions;
	}

	public void setUiOptions(String uiOptions) {
		this.uiOptions = uiOptions;
	}

	public String getImmersive() {
		return immersive;
	}

	public void setImmersive(String immersive) {
		this.immersive = immersive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWindowSoftInputMode() {
		return windowSoftInputMode;
	}

	public void setWindowSoftInputMode(String windowSoftInputMode) {
		this.windowSoftInputMode = windowSoftInputMode;
	}

	public String getHardwareAccelerated() {
		return hardwareAccelerated;
	}

	public void setHardwareAccelerated(String hardwareAccelerated) {
		this.hardwareAccelerated = hardwareAccelerated;
	}

	public String getLaunchMode() {
		return launchMode;
	}

	public void setLaunchMode(String launchMode) {
		this.launchMode = launchMode;
	}

	public String getScreenOrientation() {
		return screenOrientation;
	}

	public void setScreenOrientation(String screenOrientation) {
		this.screenOrientation = screenOrientation;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

    public boolean isUseHostPackageName() {
        return useHostPackageName;
    }

    public void setUseHostPackageName(boolean useHostPackageName) {
        this.useHostPackageName = useHostPackageName;
    }

}
