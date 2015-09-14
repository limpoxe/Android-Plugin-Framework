package com.plugin.core;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * 根据不同的rom，可能需要重写更多的方法，目前发现的几个机型的问题暂时只需要重写下面2个方法。
 * @author cailiming
 *
 */
public class PluginResourceWrapper extends Resources {

	public PluginResourceWrapper(AssetManager assets, DisplayMetrics metrics,
			Configuration config) {
		super(assets, metrics, config);
	}
	
	@Override
	public String getResourcePackageName(int resid) throws NotFoundException {
		try {
			return super.getResourcePackageName(resid);
		} catch(NotFoundException e) {
			//就目前测试的情况来看，只有Coolpad、vivo、oppo等手机会在上面抛异常，走到这里来，
			//华为、三星、小米等手机不会到这里来。
			if (isMainResId(resid)) {
				return PluginLoader.getApplicatoin().getPackageName();
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
			//就目前测试的情况来看，只有Coolpad、vivo、oppo等手机会在上面抛异常，走到这里来，
			//华为、三星、小米等手机不会到这里来。
			if (isMainResId(resid)) {
				return PluginLoader.getApplicatoin().getResources().getResourceName(resid);
			}
			throw new NotFoundException("Unable to find resource ID #0x"
	                + Integer.toHexString(resid));
		}
	}


	private static boolean isMainResId(int resid) {
		//如果使的使public.xml
		//return PluginPublicXmlConst.resourceMap.indexOfKey(resid>>16) > 0;

		//如果使用的使openatlasextention
		//默认宿主的资源id以0x7f开头
		return resid>>24 == 0x7f;
	}
     
}

