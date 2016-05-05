package com.plugin.core;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;
import com.plugin.util.ResourceUtil;

import java.lang.reflect.Field;

/**
 * 根据不同的rom，可能需要重写更多的方法，目前发现的几个机型的问题暂时只需要重写下面2个方法。
 * @author cailiming
 *
 */
public class PluginResourceWrapper extends Resources {

	private PluginDescriptor mPluginDescriptor;

	public PluginResourceWrapper(AssetManager assets, DisplayMetrics metrics,
			Configuration config, PluginDescriptor pluginDescriptor) {
		super(assets, metrics, config);
		this.mPluginDescriptor = pluginDescriptor;
	}
	
	@Override
	public String getResourcePackageName(int resid) throws NotFoundException {
		try {
			return super.getResourcePackageName(resid);
		} catch(NotFoundException e) {
			LogUtil.e("NotFoundException Try Following", Integer.toHexString(resid));

			//就目前测试的情况来看，只有Coolpad、vivo、oppo等手机会在上面抛异常，走到这里来，
			//华为、三星、小米等手机不会到这里来。
			if (ResourceUtil.isMainResId(resid)) {
				return PluginLoader.getApplication().getPackageName();
			}
			throw new NotFoundException("Unable to find resource ID #0x"
	                + Integer.toHexString(resid));
		}
	}
	
	@Override
	public String getResourceName(int resid) throws NotFoundException {
		try {
			return super.getResourceName(resid);
		} catch(NotFoundException e) {
			LogUtil.e("NotFoundException Try Following");

			//就目前测试的情况来看，只有Coolpad、vivo、oppo等手机会在上面抛异常，走到这里来，
			//华为、三星、小米等手机不会到这里来。
			if (ResourceUtil.isMainResId(resid)) {
				return PluginLoader.getApplication().getResources().getResourceName(resid);
			}
			throw new NotFoundException("Unable to find resource ID #0x"
	                + Integer.toHexString(resid));
		}
	}

	/**
	 * 重写此方法主要是为了修正在插件中通过
	 * getIdentifier(name, type,  getPackageName())此中形式反查id时
	 *
	 * 如果第三个参数通过调用getPackageName获得，由于此方法返回的是宿主的包名，可能会得不到预期的结果
	 *
	 * @param name
	 * @param defType
	 * @param defPackage
	 * @return
	 */
	@Override
	public int getIdentifier(String name, String defType, String defPackage) {

		if (TextUtils.isDigitsOnly(name)) {
			return super.getIdentifier(name, defType, defPackage);
		}

		//传了packageName，而且不是宿主的packageName， 则直接返回
		if (!TextUtils.isEmpty(defPackage) && !PluginLoader.getApplication().getPackageName().equals(defPackage)) {
			return super.getIdentifier(name, defType, defPackage);
		}

		//package:type/entry
		//第一段 “package:“ 第二段 ”type/“ 第三段 “entry”
		String packageName = null;
		String type = null;
		String entry = null;

		String[] pte = name.split(":");
		String[] te;
		if (pte.length == 2) {
			packageName = pte[0];
			te = pte[1].split("/");
		} else {
			te = pte[0].split("/");
		}

		if (te.length == 2) {
			type = te[0];
			entry = te[1];
		} else {
			entry = te[0];
		}

		if (packageName == null) {
			packageName = defPackage;
		}

		if (type == null) {
			type = defType;
		}

		if (PluginLoader.getApplication().getPackageName().equals(packageName)) {
			if (mPluginDescriptor.isStandalone()) {
				packageName = mPluginDescriptor.getPackageName();
			} else {
				// 判断是否在真的在宿主中
				Class rClass = null;
				try {
					String className = packageName + ".R$" + type;
					rClass = this.getClass().getClassLoader().loadClass(className);
					Field field = rClass.getDeclaredField(entry);
					if (field == null) {
						//不在宿主中，换成插件的
						packageName = mPluginDescriptor.getPackageName();
					}
				} catch (Exception e) {
					//不在宿主中，换成插件的
					packageName = mPluginDescriptor.getPackageName();
				}
			}
		}
		return super.getIdentifier(entry, type, packageName);

	}
}

