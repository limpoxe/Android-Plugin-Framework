package com.plugin.util;

import android.os.Build;
import android.widget.Toast;

import com.plugin.core.PluginLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileUtil {

	public static boolean copyFile(String source, String dest) {
		try {
			return copyFile(new FileInputStream(new File(source)), dest);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean copyFile(final InputStream inputStream, String dest) {
		LogUtil.d("copyFile to " + dest);
		FileOutputStream oputStream = null;
		try {
			File destFile = new File(dest);
			destFile.getParentFile().mkdirs();
			destFile.createNewFile();

			oputStream = new FileOutputStream(destFile);
			byte[] bb = new byte[48 * 1024];
			int len = 0;
			while ((len = inputStream.read(bb)) != -1) {
				oputStream.write(bb, 0, len);
			}
			oputStream.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oputStream != null) {
				try {
					oputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean copySo(File sourceDir, String so, String dest) {

		try {

			String name = "lib" + File.separator + Build.CPU_ABI + File.separator + so;
			File sourceFile = new File(sourceDir, name);

			if (!sourceFile.exists() && Build.CPU_ABI2 != null) {
				name = "lib" + File.separator + Build.CPU_ABI2 + File.separator + so;
				sourceFile = new File(sourceDir, name);

				if (!sourceFile.exists()) {
					name = "lib" + File.separator + "armeabi" + File.separator + so;
					sourceFile = new File(sourceDir, name);
				}
			}

			if (sourceFile.exists()) {
				copyFile(sourceFile.getAbsolutePath(), dest + File.separator + so);
			} else {
				Toast.makeText(PluginLoader.getApplicatoin(), "安装" + so + "失败:NO_MATCHING_ABIS", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return true;
	}


	public static Set<String> unZipSo(String apkFile, File tempDir) {

		HashSet<String> result = null;

		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}

		LogUtil.d("开始解压到", tempDir.getAbsolutePath());

		ZipFile zfile = null;
		boolean isSuccess = false;
		BufferedOutputStream fos = null;
		BufferedInputStream bis = null;
		try {
			zfile = new ZipFile(apkFile);
			ZipEntry ze = null;
			Enumeration zList = zfile.entries();
			while (zList.hasMoreElements()) {
				ze = (ZipEntry) zList.nextElement();
				String relativePath = ze.getName();

				if (!relativePath.startsWith("lib" + File.separator)) {
					LogUtil.d("不是lib目录，跳过", relativePath);
					continue;
				}

				if (ze.isDirectory()) {
					File folder = new File(tempDir, relativePath);
					LogUtil.d("正在创建目录", folder.getAbsolutePath());
					if (!folder.exists()) {
						folder.mkdirs();
					}

				} else {

					if (result == null) {
						result = new HashSet<String>(4);
					}

					File targetFile = new File(tempDir, relativePath);
					LogUtil.d("正在解压文件", targetFile.getAbsolutePath());
					if (!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();

					fos = new BufferedOutputStream(new FileOutputStream(targetFile));
					bis = new BufferedInputStream(zfile.getInputStream(ze));
					byte[] buffer = new byte[2048];
					int count = -1;
					while ((count = bis.read(buffer)) != -1) {
						fos.write(buffer, 0, count);
						fos.flush();
					}
					fos.close();
					fos = null;
					bis.close();
					bis = null;

					result.add(relativePath.substring(relativePath.lastIndexOf(File.separator) +1));
				}
			}
			isSuccess = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (zfile != null) {
				try {
					zfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		LogUtil.d("解压结束", isSuccess);
		return result;
	}

	public static void readFileFromJar(String jarFilePath, String metaInfo) {
		LogUtil.d("readFileFromJar:", jarFilePath, metaInfo);
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarFilePath);
			JarEntry entry = jarFile.getJarEntry(metaInfo);
			if (entry != null) {
				InputStream input = jarFile.getInputStream(entry);

				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return;

	}

	/**
	 * 递归删除文件及文件夹
	 * @param file
	 */
	public static boolean deleteAll(File file) {
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles != null && childFiles.length > 0) {
				for (int i = 0; i < childFiles.length; i++) {
					deleteAll(childFiles[i]);
				}
			}
		}
		return file.delete();
	}

	public static String streamToString(InputStream input) throws IOException {

		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(isr);

		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		isr.close();
		return sb.toString();
	}

}
