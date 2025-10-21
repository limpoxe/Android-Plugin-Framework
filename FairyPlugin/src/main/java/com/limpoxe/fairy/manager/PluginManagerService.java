package com.limpoxe.fairy.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginCreator;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.core.localservice.LocalServiceManager;
import com.limpoxe.fairy.util.FileUtil;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.PackageVerifyer;
import com.limpoxe.fairy.util.ProcessUtil;
import com.limpoxe.fairy.util.RefInvoker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class PluginManagerService {
    private static final String SP_NAME = "plugins.installed";
	private static final String ENABLED_KEY = "plugins.list";

	private Object mLock = new Object();
	private final Hashtable<String, PluginDescriptor> mEnabledPlugins = new Hashtable<String, PluginDescriptor>();

	PluginManagerService() {
		if (FairyGlobal.isInited()) {//防止集成了插件框架但是没有调用init导致app起不来
			if (!ProcessUtil.isPluginProcess()) {
				throw new IllegalAccessError("本类仅在插件进程使用");
			}
		} else {
			LogUtil.e("插件框架未初始化！");
			LogUtil.printStackTrace();
		}
	}

	private static String genTmpPath(File srcFile) {
		return FairyGlobal.getHostApplication().getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + "_" + srcFile.getName();
	}

	@SuppressWarnings("unchecked")
	void loadEnabledPlugins() {
		synchronized (mLock) {
			if (mEnabledPlugins.size() == 0) {
				long t1 = System.currentTimeMillis();
                Hashtable<String, PluginDescriptor> installedPlugin = readPlugins(ENABLED_KEY);
                if (installedPlugin != null) {
                    mEnabledPlugins.putAll(installedPlugin);
                }
				long t2 = System.currentTimeMillis();
				LogUtil.i("加载所有插件列表, 耗时 : " + (t2 - t1));
			}
		}
	}

	private boolean updateEnabledPlugins(PluginDescriptor pluginDescriptor) {
		mEnabledPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
        boolean isSaveSuccess = writePlugins(ENABLED_KEY, mEnabledPlugins);
        if (!isSaveSuccess) {
            mEnabledPlugins.remove(pluginDescriptor.getPackageName());
        }
        return isSaveSuccess;
	}

	boolean removeAll() {
		synchronized (mLock) {
			LogUtil.w("卸载所有插件");
			Iterator<Map.Entry<String, PluginDescriptor>> itr = mEnabledPlugins.entrySet().iterator();
			while(itr.hasNext()) {
				Map.Entry<String, PluginDescriptor> entry = itr.next();
				PluginLauncher.instance().stopPlugin(entry.getKey(), entry.getValue());
			}

			mEnabledPlugins.clear();
			boolean isSuccess = writePlugins(ENABLED_KEY, mEnabledPlugins);

			File rootDir = new File(PluginDescriptor.getFairyDir());
			LogUtil.w("删除文件夹", rootDir.getAbsolutePath());
			FileUtil.deleteAll(rootDir);
			LogUtil.w("删除完成");

			return isSuccess;
		}
	}

	int remove(String pluginId) {
		synchronized (mLock) {
			LogUtil.w("卸载插件", pluginId);
			PluginDescriptor old = mEnabledPlugins.get(pluginId);
			if (old != null) {
				PluginLauncher.instance().stopPlugin(pluginId, old);
				mEnabledPlugins.remove(pluginId);
				writePlugins(ENABLED_KEY, mEnabledPlugins);

				File dir = new File(old.getVersionedRootDir());
				LogUtil.w("删除插件目录", pluginId, dir.getAbsolutePath());
				boolean deleteSuccess = FileUtil.deleteAll(dir);
				LogUtil.e("删除完成");

				if (deleteSuccess) {
					return PluginManagerHelper.REMOVE_SUCCESS;
				} else {
					LogUtil.e("remove：REMOVE_FAIL", pluginId);
					return PluginManagerHelper.REMOVE_FAIL;
				}
			} else {
				LogUtil.e("remove：REMOVE_FAIL_PLUGIN_NOT_EXIST", pluginId);
				return PluginManagerHelper.REMOVE_FAIL_PLUGIN_NOT_EXIST;
			}
		}
	}

	Collection<PluginDescriptor> getPlugins() {
		return mEnabledPlugins.values();
	}

	/**
	 * for Fragment
	 *
	 * @param clazzId
	 * @return
	 */
	PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId) {
		Iterator<PluginDescriptor> itr = mEnabledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsFragment(clazzId)) {
				return descriptor;
			}
		}
		return null;
	}

	PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
		PluginDescriptor pluginDescriptor = mEnabledPlugins.get(pluginId);
		if (pluginDescriptor != null && pluginDescriptor.isEnabled()) {
			return pluginDescriptor;
		}
		return null;
	}

	PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		Iterator<PluginDescriptor> itr = mEnabledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsName(clazzName)) {
				return descriptor;
			}
		}
		return null;
	}

	/**
	 * 安装一个插件
	 *
	 * @param srcPluginFile
	 * @return
	 */
	InstallResult installPlugin(String srcPluginFile) {
		synchronized (mLock) {
			LogUtil.w("开始安装插件", srcPluginFile);
			long startAt = System.currentTimeMillis();
			if (TextUtils.isEmpty(srcPluginFile) || !FileUtil.checkPathSafe(srcPluginFile)) {
				LogUtil.e("fail::SRC_FILE_NOT_FOUND", srcPluginFile);
				return new InstallResult(PluginManagerHelper.SRC_FILE_NOT_FOUND);
			}
			File srcFile = new File(srcPluginFile);
			if (!srcFile.exists() || !srcFile.isFile()) {
				LogUtil.e("fail::SRC_FILE_NOT_FOUND", srcPluginFile);
				return new InstallResult(PluginManagerHelper.SRC_FILE_NOT_FOUND);
			}
			try {
				// 解析相对路径，得到真实绝对路径
				srcPluginFile = srcFile.getCanonicalPath();
			} catch (IOException e) {
				LogUtil.printException("PluginManagerService.installPlugin", e);
				LogUtil.e("fail::getCanonicalPath", srcPluginFile);
				return new InstallResult(PluginManagerHelper.INSTALL_FAIL);
			}

			// 先将apk复制到宿主程序私有目录，防止在安装过程中文件被篡改
			if (!srcPluginFile.startsWith(FairyGlobal.getHostApplication().getCacheDir().getAbsolutePath())) {
				String tempFilePath = genTmpPath(srcFile);
				LogUtil.w("先将apk复制到宿主程序私有目录", tempFilePath);
				if (!FileUtil.copyFile(srcPluginFile, tempFilePath)) {
					new File(tempFilePath).delete();
					LogUtil.e("fail::COPY_FILE_FAIL", srcPluginFile, tempFilePath);
					return new InstallResult(PluginManagerHelper.COPY_FILE_FAIL);
				}
				srcPluginFile = tempFilePath;
			}

			// 解析Manifest，获得插件详情
			LogUtil.w("解析插件Manifest", srcPluginFile);
			final PluginDescriptor pluginDescriptor = PluginManifestParser.parseManifest(srcPluginFile);
			if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getPackageName())) {
				new File(srcPluginFile).delete();
				LogUtil.e("fail::PARSE_MANIFEST_FAIL", srcPluginFile);
				return new InstallResult(PluginManagerHelper.PARSE_MANIFEST_FAIL);
			}
			pluginDescriptor.setFileSize(new File(srcPluginFile).length());

			LogUtil.w("插件信息", pluginDescriptor.getPackageName(), pluginDescriptor.getVersion(), pluginDescriptor.isStandalone(), pluginDescriptor.getAutoStart());
			if (pluginDescriptor.getPackageName().indexOf(File.separatorChar) >= 0 || pluginDescriptor.getVersion().indexOf(File.separatorChar) >= 0) {
				new File(srcPluginFile).delete();
				LogUtil.e("fail::PARSE_MANIFEST_FAIL", srcPluginFile);
				return new InstallResult(PluginManagerHelper.PARSE_MANIFEST_FAIL);
			}

			// 检查插件适用系统版本
			LogUtil.w("检查插件适用系统版本", pluginDescriptor.getMinSdkVersion(), Build.VERSION.SDK_INT);
			if (pluginDescriptor.getMinSdkVersion() != null && Build.VERSION.SDK_INT < Integer.valueOf(pluginDescriptor.getMinSdkVersion()))  {
				new File(srcPluginFile).delete();
				LogUtil.e("fail::MIN_API_NOT_SUPPORTED", pluginDescriptor.getPackageName(), "系统:" + Build.VERSION.SDK_INT, "插件:" + pluginDescriptor.getMinSdkVersion());
				return new InstallResult(PluginManagerHelper.MIN_API_NOT_SUPPORTED, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			}

			// 验证插件APK签名，如果被篡改过，将获取不到证书
			// 之所以把验证签名步骤在放在验证适用系统版本之后，
			// 是因为不同的minSdkVersion在签名时使用的sha算法长度不同，
			// 也即高版本的minSdkVersion的插件，即使签名没有被篡改过，在低版本的系统中仍然会校验失败
			// 所以先校验minSdkVersion，再校验签名
			LogUtil.w("读取插件APK签名");
			Signature[] pluginSignatures = FairyGlobal.getHostApplication().getPackageManager().getPackageArchiveInfo(srcPluginFile, PackageManager.GET_SIGNATURES).signatures;
			if (pluginSignatures == null) {
				new File(srcPluginFile).delete();
				LogUtil.e("fail::SIGNATURES_INVALIDATE", srcPluginFile);
				return new InstallResult(PluginManagerHelper.SIGNATURES_INVALIDATE);
			}

			// 可选步骤，验证插件APK证书是否和宿主程序证书相同。
			// 证书中存放的是公钥和算法信息，而公钥和私钥是1对1的
			// 公钥相同意味着是同一个作者发布的程序
			LogUtil.w("检查插件和宿主签名（调用FairyGlobal.setNeedVerifyPlugin()可关闭检查）");
			boolean debuggable = (0 != (FairyGlobal.getHostApplication().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
			if (FairyGlobal.isNeedVerifyPlugin() && !debuggable) {
				Signature[] mainSignatures = null;
				try {
					PackageInfo pkgInfo = FairyGlobal.getHostApplication().getPackageManager().getPackageInfo(FairyGlobal.getHostApplication().getPackageName(), PackageManager.GET_SIGNATURES);
					mainSignatures = pkgInfo.signatures;
				} catch (PackageManager.NameNotFoundException e) {
					LogUtil.printException("PluginManagerService.installPlugin", e);
				}
				if (!PackageVerifyer.isSignaturesSame(mainSignatures, pluginSignatures)) {
					new File(srcPluginFile).delete();
					LogUtil.e("fail::VERIFY_SIGNATURES_FAIL", srcPluginFile);
					return new InstallResult(PluginManagerHelper.VERIFY_SIGNATURES_FAIL);
				}
			}

			// 检查当前宿主版本是否匹配此非独立插件需要的版本
			LogUtil.w("检查插件和宿主是否兼容");
			if (!PackageVerifyer.isCompatibleWithHost(pluginDescriptor)) {
				//不满足要求，不可安装此插件
				new File(srcPluginFile).delete();
				LogUtil.e("fail::HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN", pluginDescriptor.getPackageName());
				return new InstallResult(PluginManagerHelper.HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			}

			boolean isOldRunning = false;
			boolean isSameVersion = false;
			PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
			if (oldPluginDescriptor != null) {
				// 检查现有插件版本和要更新的插件版本是否相同
				LogUtil.w("检查插件版本是否有变化（调用FairyGlobal.setInstallationWithSameVersion()可关闭坚持）");
				isOldRunning = PluginLauncher.instance().isRunning(oldPluginDescriptor.getPackageName());
				isSameVersion = oldPluginDescriptor.getVersion().equals(pluginDescriptor.getVersion());
				//版本号无变化，不需要安装
				if (!FairyGlobal.isInstallationWithSameVersion() && isSameVersion) {
					new File(srcPluginFile).delete();
					LogUtil.e("fail::SAME_VERSION", oldPluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
					return new InstallResult(PluginManagerHelper.FAIL_BECAUSE_SAME_VER_HAS_LOADED, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
				}
			}

			final String destApkPath = pluginDescriptor.getInstalledPath();
			LogUtil.w("复制插件到插件目录", destApkPath);
			boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destApkPath);
			new File(srcPluginFile).delete();
			srcPluginFile = null;
			if (!isCopySuccess) {
				LogUtil.e("fail::COPY_FILE_FAIL", destApkPath);
				return new InstallResult(PluginManagerHelper.COPY_FILE_FAIL, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			}

			pluginDescriptor.setInstallationTime(System.currentTimeMillis());
			PackageInfo packageInfo = pluginDescriptor.getPackageInfo(PackageManager.GET_GIDS);
			if (packageInfo != null) {
				LogUtil.v("设置theme、logo、icon", pluginDescriptor.getInstalledPath());
				pluginDescriptor.setApplicationTheme(packageInfo.applicationInfo.theme);
				pluginDescriptor.setApplicationIcon(packageInfo.applicationInfo.icon);
				pluginDescriptor.setApplicationLogo(packageInfo.applicationInfo.logo);
			}

			// 先解压so到临时目录，再从临时目录复制到插件so目录。 在构造插件Dexclassloader的时候，会使用这个so目录作为参数
			File tempUnzipDir = new File(pluginDescriptor.getVersionedRootDir(), "temp");
			Set<String> soList = FileUtil.unZipSo(pluginDescriptor.getInstalledPath(), tempUnzipDir);
			if (soList != null) {//TODO soList插件中所有so的名字列表，如果插件中不同cpu架构下的so个数不相等可能会复制不匹配的so
				ArrayList<String> abiList = getSupportedAbis();
				LogUtil.w("复制so", pluginDescriptor.getNativeLibDir());
				for (String soName : soList) {
					FileUtil.copySo2(tempUnzipDir, soName, pluginDescriptor.getNativeLibDir(), abiList);
				}
				//删掉临时文件
				LogUtil.v("删除so的临时解压目录", tempUnzipDir);
				FileUtil.deleteAll(tempUnzipDir);
				LogUtil.v("删除完成");
			}

			//try {
			//ArrayList<String> multiDexFiles = PluginMultiDexExtractor.performExtractions(new File(destApkPath), new File(apkParent, "secondDexes"));
			//pluginDescriptor.setMuliDexList(multiDexFiles);
			//} catch (IOException e) {
			//	e.printStackTrace();
			//}

			File dalvikCacheDir = new File(pluginDescriptor.getDalvikCacheDir());
			LogUtil.v("删除DEXOPT缓存目录", dalvikCacheDir.getAbsolutePath());
			FileUtil.deleteAll(dalvikCacheDir);
			LogUtil.v("删除完成", dalvikCacheDir.getAbsolutePath());

			LogUtil.v("触发DEXOPT...", pluginDescriptor.getInstalledPath());
			//ActivityThread.getPackageManager().performDexOptIfNeeded()
			ClassLoader cl = PluginCreator.createPluginClassLoader(
					pluginDescriptor.getPackageName(),
					pluginDescriptor.getInstalledPath(),
					pluginDescriptor.getDalvikCacheDir(),
					pluginDescriptor.getNativeLibDir(),
					pluginDescriptor.isStandalone(),
					null,
					null);
			try {
				cl.loadClass(Object.class.getName());
				cl = null;
			} catch (ClassNotFoundException e) {
				LogUtil.printException("PluginManagerService.installPlugin", e);
			}
			LogUtil.v("DEXOPT完毕");
			//打印一下目录结构
			if (debuggable) {
				FileUtil.printAll(new File(FairyGlobal.getHostApplication().getApplicationInfo().dataDir));
			}

			//这个步骤真正完成插件更替
			LogUtil.v("开始插件更替", pluginDescriptor.getPackageName());
			//如果版本相同，不能删除。因为版本相同时新版本和旧版本的安装目录是同一个
			if (!isSameVersion) {
				remove(pluginDescriptor.getPackageName());
			}
			boolean isInstallSuccess = updateEnabledPlugins(pluginDescriptor);
			LogUtil.v("结束插件更替", pluginDescriptor.getPackageName(), isInstallSuccess);
			if (!isInstallSuccess) {
				LogUtil.e("fail::INSTALL_FAIL", pluginDescriptor.getPackageName());
				return new InstallResult(PluginManagerHelper.INSTALL_FAIL, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
			}
			LogUtil.w("安装" + pluginDescriptor.getPackageName() + "成功，耗时(ms) : " + (System.currentTimeMillis() - startAt));

			LogUtil.v("注册插件内定义的localService");
			LocalServiceManager.registerService(pluginDescriptor);
			LogUtil.v("注册完成");

			//如果是自启动插件，或者安装时旧版本插件正处于运行中，则新版本安装完成后立即启动
			if (pluginDescriptor.getAutoStart() || isOldRunning) {
				postWakeup(pluginDescriptor);
			}
			return new InstallResult(PluginManagerHelper.SUCCESS, pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
		}
	}

	private void postWakeup(final PluginDescriptor pluginDescriptor) {
		LogUtil.w("唤醒插件", pluginDescriptor.getPackageName());
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				boolean succ = PluginManagerHelper.wakeup(pluginDescriptor.getPackageName());
				LogUtil.w("立即唤醒" + (succ?"成功":"失败"), pluginDescriptor.getPackageName());
			}
		});
	}

	private static ArrayList<String> getSupportedAbis() {

		ArrayList<String> abiList = new ArrayList<>();

		String defaultAbi = (String) RefInvoker.getField(FairyGlobal.getHostApplication().getApplicationInfo(), ApplicationInfo.class, "primaryCpuAbi");
		abiList.add(defaultAbi);

		if (Build.VERSION.SDK_INT >= 21) {
			String[] abis = Build.SUPPORTED_ABIS;
			if (abis != null) {
				for (String abi: abis) {
					abiList.add(abi);
				}
			}
		} else {
			abiList.add(Build.CPU_ABI);
			abiList.add(Build.CPU_ABI2);
			abiList.add("armeabi");
		}
		return abiList;
	}

	private static SharedPreferences getSharedPreference() {
		SharedPreferences sp = FairyGlobal.getHostApplication().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		return sp;
	}

	private boolean writePlugins(String key, Hashtable<String, PluginDescriptor> plugins) {

		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(plugins);
			objectOutputStream.flush();

			byte[] data = byteArrayOutputStream.toByteArray();
			String list = Base64.encodeToString(data, Base64.DEFAULT);

			getSharedPreference().edit().putString(key, list).commit();
			return true;
		} catch (Exception e) {
			LogUtil.printException("PluginManagerService.savePlugins", e);
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					LogUtil.printException("PluginManagerService.savePlugins", e);
				}
			}
			if (byteArrayOutputStream != null) {
				try {
					byteArrayOutputStream.close();
				} catch (IOException e) {
					LogUtil.printException("PluginManagerService.savePlugins", e);
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Hashtable<String, PluginDescriptor> readPlugins(String key) {
		String list = getSharedPreference().getString(key, "");
		Serializable object = null;
		if (!TextUtils.isEmpty(list)) {
			ByteArrayInputStream byteArrayInputStream = null;
			ObjectInputStream objectInputStream = null;
			try {
				byteArrayInputStream = new ByteArrayInputStream(Base64.decode(list, Base64.DEFAULT));
				objectInputStream = new ObjectInputStream(byteArrayInputStream);
				object = (Serializable) objectInputStream.readObject();
			} catch (Exception e) {
				LogUtil.printException("PluginManagerService.readPlugins", e);
			} finally {
				if (objectInputStream != null) {
					try {
						objectInputStream.close();
					} catch (IOException e) {
						LogUtil.printException("PluginManagerService.readPlugins", e);
					}
				}
				if (byteArrayInputStream != null) {
					try {
						byteArrayInputStream.close();
					} catch (IOException e) {
						LogUtil.printException("PluginManagerService.readPlugins", e);
					}
				}
			}
		}

		return (Hashtable<String, PluginDescriptor>) object;
	}

}