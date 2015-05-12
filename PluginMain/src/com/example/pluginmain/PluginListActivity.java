package com.example.pluginmain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.plugin.core.PluginDescriptor;
import com.plugin.core.PluginLoader;
import com.plugin.util.ApkReader;
import com.plugin.util.RefInvoker;

import dalvik.system.DexClassLoader;

public class PluginListActivity extends Activity {

	private ViewGroup mList;

	public static class MyLoader extends DexClassLoader {

		public MyLoader(String dexPath, String optimizedDirectory,
				String libraryPath, ClassLoader parent) {
			super(dexPath, optimizedDirectory, libraryPath, parent);
		}
		
		@Override
		protected Class<?> loadClass(String className, boolean resolve)
				throws ClassNotFoundException {
			
			if (className.equals(PluginStubActivity.class.getName())) {
				return PluginLoader.loadPluginClassById("test5");
			}
			
			return super.loadClass(className, resolve);
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// 这行代码应当在Application的onCreate中执行。
		PluginLoader.initLoader(getApplication());

		registerReceiver(pluginChange, new IntentFilter(PluginLoader.ACTION_PLUGIN_CHANGED));

		Button install = (Button) findViewById(R.id.install);
		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(PluginListActivity.this, "开始安装", Toast.LENGTH_LONG).show();
				try {
					InputStream assestInput = getAssets().open("PluginTest-debug.apk");
					String sdcardDest = Environment.getExternalStorageDirectory().getAbsolutePath()
							+ "/PluginTest-debug.apk";
					if (ApkReader.copyFile(assestInput, sdcardDest)) {
						PluginLoader.installPlugin(sdcardDest);
					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(PluginListActivity.this, "安装失败", Toast.LENGTH_LONG).show();
				}
			}
		});

		mList = (ViewGroup) findViewById(R.id.list);

		Button open = (Button) findViewById(R.id.open);
		open.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//替换成可以加载 插件元素test5 的classLoader
				replaceClassLoader();
				
				//打开stubActivity实际上会打开插件中test5对应的activity
				Intent targent = new Intent(PluginListActivity.this, PluginStubActivity.class);
				startActivity(targent);
			}
		});
		
		listAll(mList);
	}
	
	private void replaceClassLoader() {
		Object mLoadedApk = RefInvoker.getFieldObject(PluginLoader.getApplicatoin(), Application.class.getName(), "mLoadedApk");
		
		ClassLoader originalLoader = (ClassLoader) RefInvoker.getFieldObject(
				mLoadedApk, "android.app.LoadedApk", "mClassLoader");
		
		DexClassLoader dLoader = new MyLoader("", PluginLoader.getApplicatoin().getCacheDir()
				.getAbsolutePath(), PluginLoader.getApplicatoin().getCacheDir().getAbsolutePath(),
				originalLoader);

		RefInvoker.setFieldObject(mLoadedApk, "android.app.LoadedApk",
				"mClassLoader", dLoader);
	}
	
	private void listAll(ViewGroup root) {
		root.removeAllViews();

		// 列出所有已经安装的插件
		Hashtable<String, PluginDescriptor> plugins = PluginLoader.listAll();
		Iterator<Entry<String, PluginDescriptor>> itr = plugins.entrySet().iterator();
		while (itr.hasNext()) {
			final Entry<String, PluginDescriptor> entry = itr.next();
			Button btton = new Button(this);
			btton.setText("插件id：" + entry.getKey() + "\n插件介绍：" + entry.getValue().getDescription() + "\n安装路径："
					+ entry.getValue().getInstalledPath() + "\n点击查看详情>>");
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 10;
			layoutParam.bottomMargin = 10;
			root.addView(btton, layoutParam);

			btton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PluginListActivity.this, PluginDetailActivity.class);
					intent.putExtra("plugin_id", entry.getKey());
					startActivity(intent);
				}
			});
		}
	}

	private final BroadcastReceiver pluginChange = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(PluginListActivity.this, "插件" + intent.getStringExtra(PluginLoader.EXTRA_TYPE) + "成功",
					Toast.LENGTH_LONG).show();
			listAll(mList);
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginChange);
	};

}
