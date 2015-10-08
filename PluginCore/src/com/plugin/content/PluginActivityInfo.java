package com.plugin.content;

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
	private String launchMode;//string
	private String screenOrientation;//string
	private String theme;//int
	private String immersive;//int string

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
}
