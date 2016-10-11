package com.plugin.core;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.plugin.content.PluginDescriptor;
import com.plugin.util.LogUtil;
import com.plugin.util.ResourceUtil;

import java.util.HashSet;

/**
 * 根据不同的rom，可能需要重写更多的方法，目前发现的几个机型的问题暂时只需要重写下面2个方法。
 * @author cailiming
 *
 */
public class PluginResourceWrapper extends Resources {

	private HashSet<Integer> idCaches = new HashSet<>(5);

	private PluginDescriptor mPluginDescriptor;

	public PluginResourceWrapper(AssetManager assets, DisplayMetrics metrics,
			Configuration config, PluginDescriptor pluginDescriptor) {
		super(assets, metrics, config);
		this.mPluginDescriptor = pluginDescriptor;
	}
	
	@Override
	public String getResourcePackageName(int resid) throws NotFoundException {
		if (idCaches.contains(resid)) {
			return PluginLoader.getApplication().getPackageName();
		}
		try {
			return super.getResourcePackageName(resid);
		} catch(NotFoundException e) {
			LogUtil.e("NotFoundException Try Following", Integer.toHexString(resid));

			//就目前测试的情况来看，只有Coolpad、vivo、oppo等手机会在上面抛异常，走到这里来，
			//华为、三星、小米等手机不会到这里来。
			if (ResourceUtil.isMainResId(resid)) {
				idCaches.add(resid);
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

//	现已支持getPackageName返回插件包名,删除此方法
//	@Override
//	public int getIdentifier(String name, String defType, String defPackage) {
//		//XXX
//		//XXX
//	}
}

