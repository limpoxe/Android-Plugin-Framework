package com.limpoxe.fairy.content;

import java.io.Serializable;
/**
 * <Pre>
 * @author cailiming
 * </Pre>
 *
 * Copy from Android SDK
 *
 */
public class PluginProviderInfo implements Serializable {

	private String name;

	private String packageName;

	private String processName;

	private boolean enabled = true;

	private boolean exported = false;

	private String authority = null;

	private String readPermission = null;

	private String writePermission = null;

	private boolean grantUriPermissions = false;

	private boolean multiprocess = false;

	private int initOrder = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getReadPermission() {
		return readPermission;
	}

	public void setReadPermission(String readPermission) {
		this.readPermission = readPermission;
	}

	public String getWritePermission() {
		return writePermission;
	}

	public void setWritePermission(String writePermission) {
		this.writePermission = writePermission;
	}

	public boolean isGrantUriPermissions() {
		return grantUriPermissions;
	}

	public void setGrantUriPermissions(boolean grantUriPermissions) {
		this.grantUriPermissions = grantUriPermissions;
	}

	public boolean isMultiprocess() {
		return multiprocess;
	}

	public void setMultiprocess(boolean multiprocess) {
		this.multiprocess = multiprocess;
	}

	public int getInitOrder() {
		return initOrder;
	}

	public void setInitOrder(int initOrder) {
		this.initOrder = initOrder;
	}
}
