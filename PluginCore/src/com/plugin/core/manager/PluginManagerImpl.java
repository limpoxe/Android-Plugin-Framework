package com.plugin.core.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.plugin.content.PluginDescriptor;
import com.plugin.core.PluginCreator;
import com.plugin.core.PluginLoader;
import com.plugin.core.PluginManifestParser;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.PackageVerifyer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PluginManagerImpl implements PluginManager {

	private static final boolean NEED_VERIFY_CERT = true;
	private static final int SUCCESS = 0;
	private static final int SRC_FILE_NOT_FOUND = 1;
	private static final int COPY_FILE_FAIL = 2;
	private static final int SIGNATURES_INVALIDATE = 3;
	private static final int VERIFY_SIGNATURES_FAIL = 4;
	private static final int PARSE_MANIFEST_FAIL = 5;
	private static final int FAIL_BECAUSE_HAS_LOADED = 6;
	private static final int INSTALL_FAIL = 7;

	private static final String INSTALLED_KEY = "plugins.list";

	private static final String PENDING_KEY = "plugins.pending";

	private final Hashtable<String, PluginDescriptor> sInstalledPlugins = new Hashtable<String, PluginDescriptor>();

	private final Hashtable<String, PluginDescriptor> sPendingPlugins = new Hashtable<String, PluginDescriptor>();

	private PluginCallback changeListener = new PluginCallbackImpl();

	/**
	 * 插件的安装目录, 插件apk将来会被放在这个目录下面
	 */
	private String genInstallPath(String pluginId, String pluginVersoin) {
		return PluginLoader.getApplicatoin().getDir("plugin_dir", Context.MODE_PRIVATE).getAbsolutePath() + "/" + pluginId + "/"
				+ pluginVersoin + "/base-1.apk";
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void loadInstalledPlugins() {
		if (sInstalledPlugins.size() == 0) {
			Hashtable<String, PluginDescriptor> installedPlugin = readPlugins(INSTALLED_KEY);
			if (installedPlugin != null) {
				sInstalledPlugins.putAll(installedPlugin);
			}

			//把pending合并到install
			Hashtable<String, PluginDescriptor> pendingPlugin = readPlugins(PENDING_KEY);
			if (pendingPlugin != null) {
				Iterator<Map.Entry<String, PluginDescriptor>> itr = pendingPlugin.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, PluginDescriptor> entry = itr.next();
					//删除旧版
					remove(entry.getKey());
				}

				//保存新版
				sInstalledPlugins.putAll(pendingPlugin);
				savePlugins(INSTALLED_KEY, sInstalledPlugins);

				//清除pending
				getSharedPreference().edit().remove(PENDING_KEY).commit();
			}
		}
	}

	private boolean addOrReplace(PluginDescriptor pluginDescriptor) {
		sInstalledPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
		return savePlugins(INSTALLED_KEY, sInstalledPlugins);
	}

	private boolean pending(PluginDescriptor pluginDescriptor) {
		sPendingPlugins.put(pluginDescriptor.getPackageName(), pluginDescriptor);
		return savePlugins(PENDING_KEY, sPendingPlugins);
	}

	@Override
	public synchronized boolean removeAll() {
		sInstalledPlugins.clear();
		boolean isSuccess = savePlugins(INSTALLED_KEY, sInstalledPlugins);

		changeListener.onPluginRemoveAll();

		return isSuccess;
	}

	@Override
	public synchronized boolean remove(String pluginId) {
		PluginDescriptor old = sInstalledPlugins.remove(pluginId);
		boolean result = false;
		if (old != null) {
			result = savePlugins(INSTALLED_KEY, sInstalledPlugins);
			boolean deleteSuccess = FileUtil.deleteAll(new File(old.getInstalledPath()).getParentFile());
			LogUtil.d("delete old", result, deleteSuccess, old.getInstalledPath(), old.getPackageName());
		}

		changeListener.onPluginRemoved(pluginId);

		return result;
	}

	@Override
	public Collection<PluginDescriptor> getPlugins() {
		return sInstalledPlugins.values();
	}

	@Override
	public synchronized void enablePlugin(String pluginId, boolean enable) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && !pluginDescriptor.isEnabled()) {
			pluginDescriptor.setEnabled(enable);
			savePlugins(INSTALLED_KEY, sInstalledPlugins);
		}
	}

	/**
	 * for Fragment
	 *
	 * @param clazzId
	 * @return
	 */
	@Override
	public PluginDescriptor getPluginDescriptorByFragmenetId(String clazzId) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
		while (itr.hasNext()) {
			PluginDescriptor descriptor = itr.next();
			if (descriptor.containsFragment(clazzId)) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public PluginDescriptor getPluginDescriptorByPluginId(String pluginId) {
		PluginDescriptor pluginDescriptor = sInstalledPlugins.get(pluginId);
		if (pluginDescriptor != null && pluginDescriptor.isEnabled()) {
			return pluginDescriptor;
		}
		return null;
	}

	@Override
	public PluginDescriptor getPluginDescriptorByClassName(String clazzName) {
		Iterator<PluginDescriptor> itr = sInstalledPlugins.values().iterator();
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
	public synchronized int installPlugin(String srcPluginFile) {
		LogUtil.e("开始安装插件", srcPluginFile);
		if (TextUtils.isEmpty(srcPluginFile) || !new File(srcPluginFile).exists()) {
			return SRC_FILE_NOT_FOUND;
		}

		//第0步，先将apk复制到宿主程序私有目录，防止在安装过程中文件被篡改
		if (!srcPluginFile.startsWith(PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath())) {
			String tempFilePath = PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath()
					+ File.separator + System.currentTimeMillis() + ".apk";
			if (FileUtil.copyFile(srcPluginFile, tempFilePath)) {
				srcPluginFile = tempFilePath;
			} else {
				LogUtil.e("复制插件文件失败", srcPluginFile, tempFilePath);
				return COPY_FILE_FAIL;
			}
		}

		// 第1步，验证插件APK签名，如果被篡改过，将获取不到证书
		//sApplication.getPackageManager().getPackageArchiveInfo(srcPluginFile, PackageManager.GET_SIGNATURES);
		Signature[] pluginSignatures = PackageVerifyer.collectCertificates(srcPluginFile, false);
		boolean isDebugable = (0 != (PluginLoader.getApplicatoin().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		if (pluginSignatures == null) {
			LogUtil.e("插件签名验证失败", srcPluginFile);
			new File(srcPluginFile).delete();
			return SIGNATURES_INVALIDATE;
		} else if (NEED_VERIFY_CERT && !isDebugable) {
			//可选步骤，验证插件APK证书是否和宿主程序证书相同。
			//证书中存放的是公钥和算法信息，而公钥和私钥是1对1的
			//公钥相同意味着是同一个作者发布的程序
			Signature[] mainSignatures = null;
			try {
				PackageInfo pkgInfo = PluginLoader.getApplicatoin().getPackageManager().getPackageInfo(PluginLoader.getApplicatoin().getPackageName(), PackageManager.GET_SIGNATURES);
				mainSignatures = pkgInfo.signatures;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			if (!PackageVerifyer.isSignaturesSame(mainSignatures, pluginSignatures)) {
				LogUtil.e("插件证书和宿主证书不一致", srcPluginFile);
				new File(srcPluginFile).delete();
				return VERIFY_SIGNATURES_FAIL;
			}
		}

		// 第2步，解析Manifest，获得插件详情
		PluginDescriptor pluginDescriptor = PluginManifestParser.parseManifest(srcPluginFile);
		if (pluginDescriptor == null || TextUtils.isEmpty(pluginDescriptor.getPackageName())) {
			LogUtil.e("解析插件Manifest文件失败", srcPluginFile);
			new File(srcPluginFile).delete();
			return PARSE_MANIFEST_FAIL;
		}

		PackageInfo packageInfo = PluginLoader.getApplicatoin().getPackageManager().getPackageArchiveInfo(srcPluginFile, PackageManager.GET_GIDS);
		if (packageInfo != null) {
			pluginDescriptor.setApplicationTheme(packageInfo.applicationInfo.theme);
			pluginDescriptor.setApplicationIcon(packageInfo.applicationInfo.icon);
			pluginDescriptor.setApplicationLogo(packageInfo.applicationInfo.logo);
		}

		boolean isNeedPending = false;
		// 第3步，检查插件是否已经存在,若存在删除旧的
		PluginDescriptor oldPluginDescriptor = getPluginDescriptorByPluginId(pluginDescriptor.getPackageName());
		if (oldPluginDescriptor != null) {
			LogUtil.e("已安装过，安装路径为", oldPluginDescriptor.getInstalledPath(), oldPluginDescriptor.getVersion(), pluginDescriptor.getVersion());

			//检查插件是否已经加载
			if (oldPluginDescriptor.getPluginContext() != null) {
				if (!oldPluginDescriptor.getVersion().equals(pluginDescriptor.getVersion())) {
					LogUtil.e("旧版插件已经加载， 且新版插件和旧版插件版本不同，进入pending状态，新版插件将在安装后进程重启再生效");
					isNeedPending = true;
				} else {
					LogUtil.e("旧版插件已经加载， 且新版插件和旧版插件版本相同，拒绝安装");
					new File(srcPluginFile).delete();
					return FAIL_BECAUSE_HAS_LOADED;
				}
			} else {
				LogUtil.e("旧版插件还未加载，忽略版本，直接删除旧版，尝试安装新版");
				remove(oldPluginDescriptor.getPackageName());
			}
		}

		// 第4步骤，复制插件到插件目录
		String destApkPath = genInstallPath(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
		boolean isCopySuccess = FileUtil.copyFile(srcPluginFile, destApkPath);

		if (!isCopySuccess) {

			LogUtil.e("复制插件到安装目录失败", srcPluginFile);
			//删掉临时文件
			new File(srcPluginFile).delete();
			return COPY_FILE_FAIL;
		} else {

			//第5步，先解压so到临时目录，再从临时目录复制到插件so目录。 在构造插件Dexclassloader的时候，会使用这个so目录作为参数
			File apkParent = new File(destApkPath).getParentFile();
			File tempSoDir = new File(apkParent, "temp");
			Set<String> soList = FileUtil.unZipSo(srcPluginFile, tempSoDir);
			if (soList != null) {
				for (String soName : soList) {
					FileUtil.copySo(tempSoDir, soName, apkParent.getAbsolutePath());
				}
				//删掉临时文件
				FileUtil.deleteAll(tempSoDir);
			}

			// 第6步 添加到已安装插件列表
			pluginDescriptor.setInstalledPath(destApkPath);
			boolean isInstallSuccess = false;
			if (!isNeedPending) {
				isInstallSuccess = addOrReplace(pluginDescriptor);
			} else {
				isInstallSuccess = pending(pluginDescriptor);
			}
			//删掉临时文件
			new File(srcPluginFile).delete();

			if (!isInstallSuccess) {
				LogUtil.e("安装插件失败", srcPluginFile);

				new File(destApkPath).delete();

				return INSTALL_FAIL;
			} else {
				//通过创建classloader来触发dexopt，但不加载
				LogUtil.d("正在进行DEXOPT...", pluginDescriptor.getInstalledPath());
				//ActivityThread.getPackageManager().performDexOptIfNeeded()
				FileUtil.deleteAll(new File(apkParent, "dalvik-cache"));
				PluginCreator.createPluginClassLoader(pluginDescriptor.getInstalledPath(), pluginDescriptor.isStandalone(), null);
				LogUtil.d("DEXOPT完毕");

				if (!isNeedPending) {
					LocalServiceManager.registerService(pluginDescriptor);
				}

				changeListener.onPluginInstalled(pluginDescriptor.getPackageName(), pluginDescriptor.getVersion());
				LogUtil.e("安装插件成功," + (isNeedPending ? " 重启进程生效" : " 立即生效"), destApkPath);

				//打印一下目录结构
				FileUtil.printAll(new File(PluginLoader.getApplicatoin().getApplicationInfo().dataDir));

				return SUCCESS;
			}
		}
	}

	private static SharedPreferences getSharedPreference() {
		SharedPreferences sp = PluginLoader.getApplicatoin().getSharedPreferences("plugins.installed",
				Build.VERSION.SDK_INT < 11 ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | 0x0004);
		return sp;
	}

	private synchronized boolean savePlugins(String key, Hashtable<String, PluginDescriptor> plugins) {

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
			e.printStackTrace();
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (byteArrayOutputStream != null) {
				try {
					byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private synchronized Hashtable<String, PluginDescriptor> readPlugins(String key) {
		String list = getSharedPreference().getString(key, "");
		Serializable object = null;
		if (!TextUtils.isEmpty(list)) {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					Base64.decode(list, Base64.DEFAULT));
			ObjectInputStream objectInputStream = null;
			try {
				objectInputStream = new ObjectInputStream(byteArrayInputStream);
				object = (Serializable) objectInputStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (objectInputStream != null) {
					try {
						objectInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (byteArrayInputStream != null) {
					try {
						byteArrayInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return (Hashtable<String, PluginDescriptor>) object;
	}

}