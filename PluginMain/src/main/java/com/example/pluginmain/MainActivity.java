package com.example.pluginmain;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.example.pluginsharelib.SharePOJO;
import com.example.pluginsharelib.ShareService;
import com.example.plugintest.IMyAidlInterface;
import com.plugin.content.PluginDescriptor;
import com.plugin.core.localservice.LocalServiceManager;
import com.plugin.core.manager.PluginCallback;
import com.plugin.core.manager.PluginManagerHelper;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;
import com.plugin.util.ResourceUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

	private ViewGroup mList;
	private Button install;
	boolean isInstalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		setTitle("插件列表");

		initView();

		listAll();

		// 监听插件安装 安装新插件后刷新当前页面
		registerReceiver(pluginInstallEvent, new IntentFilter(PluginCallback.ACTION_PLUGIN_CHANGED));

//		//测试利用Action打开在宿主中唤起插件receiver
//		Intent intent = new Intent("test.rst2");//两个Receive都配置了这个aciton，这里可以同时唤起两个Receiver
//		intent.putExtra("testParam", "testParam");
//		sendBroadcast(intent);

		//测试通过宿主service唤起插件service
		startService(new Intent(this, MainService.class));
	}

	private void initView() {
		mList = (ViewGroup) findViewById(R.id.list);
		install = (Button) findViewById(R.id.install);

		final Handler handler = new Handler();

		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isInstalled) {
					isInstalled = true;

					try {
						String[] files = getAssets().list("");
						for (String apk : files) {
							if (apk.endsWith(".apk")) {
								copyAndInstall(apk);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.this, "点1次就可以啦！", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void copyAndInstall(String name) {
		try {
			InputStream assestInput = getAssets().open(name);
			String dest = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name;
			if (FileUtil.copyFile(assestInput, dest)) {
				PluginManagerHelper.installPlugin(dest);
			} else {
				assestInput = getAssets().open(name);
				dest = getCacheDir().getAbsolutePath() + "/" + name;
				if (FileUtil.copyFile(assestInput, dest)) {
					PluginManagerHelper.installPlugin(dest);
				} else {
					Toast.makeText(MainActivity.this, "解压Apk失败" + dest, Toast.LENGTH_LONG).show();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_LONG).show();
		}
	}

	private void listAll() {
		ViewGroup root = mList;
		root.removeAllViews();
		// 列出所有已经安装的插件
		Collection<PluginDescriptor> plugins = PluginManagerHelper.getPlugins();
		Iterator<PluginDescriptor> itr = plugins.iterator();
		while (itr.hasNext()) {
			final PluginDescriptor pluginDescriptor = itr.next();
			Button button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);

			LogUtil.d("插件id：", pluginDescriptor.getPackageName());

			button.setText("打开插件：" + ResourceUtil.getLabel(pluginDescriptor) + ", V" + pluginDescriptor.getVersion());
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pluginDescriptor.getPackageName());
					if (launchIntent == null) {
						Toast.makeText(MainActivity.this, "插件"  + pluginDescriptor.getPackageName() + "没有配置Launcher", Toast.LENGTH_SHORT).show();
						//没有找到Launcher，打开插件详情
						Intent intent = new Intent(MainActivity.this, DetailActivity.class);
						intent.putExtra("plugin_id", pluginDescriptor.getPackageName());
						startActivity(intent);
					} else {
						//打开插件的Launcher界面
						if (!pluginDescriptor.isStandalone()) {
							//测试向非独立插件传宿主中定义的VO对象
							launchIntent.putExtra("paramVO", new SharePOJO("宿主传过来的测试VO"));
						}
						startActivity(launchIntent);
					}

					//也可以直接构造Intent，指定打开插件中的某个Activity
					//Intent intent = new Intent("test.abc");
					//startActivity(intent);
				}
			});
		}

		if (plugins.size() >0) {
			Button button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("打开皮肤测试");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TestSkinActivity.class);
					startActivity(intent);
				}
			});

			button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("打开控件级插件测试");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TestViewActivity.class);
					startActivity(intent);
				}
			});

			button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("测试插件Service AIDL");

			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (scn == null) {
						scn = new ServiceConnection() {
							@Override
							public void onServiceConnected(ComponentName name, IBinder service) {
								IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
								try {
									iMyAidlInterface.basicTypes(1, 2L, true, 0.1f, 0.01d, "测试插件AIDL");
									Toast.makeText(MainActivity.this, "onServiceConnected", Toast.LENGTH_LONG).show();
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onServiceDisconnected(ComponentName name) {

							}
						};
					}
					bindService(new Intent("test.lmn"), scn, Context.BIND_AUTO_CREATE);

					//这里顺带测试一下localservice的跨进程效果
					ShareService ss = (ShareService)LocalServiceManager.getService("share_service");
					if (ss != null) {
						SharePOJO pojo = ss.doSomething("测试跨进程localservice");
						Toast.makeText(MainActivity.this, pojo.name, Toast.LENGTH_LONG).show();
					}
				}
			});

			button = new Button(this);
			button.setPadding(10, 25, 10, 25);
			layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParam.topMargin = 25;
			layoutParam.bottomMargin = 25;
			layoutParam.gravity = Gravity.LEFT;
			root.addView(button, layoutParam);
			button.setText("测试宿主tabActiviyt内嵌插件Activity");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TestTabActivity.class);
					startActivity(intent);
				}
			});

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginInstallEvent);

		if (scn != null) {
			unbindService(scn);
			scn = null;
		}
	};

	private ServiceConnection scn;

	private final BroadcastReceiver pluginInstallEvent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MainActivity.this,
					"插件"  + intent.getStringExtra("id") + " "+ intent.getStringExtra("type") + "完成",
					Toast.LENGTH_SHORT).show();
			listAll();
		};
	};

	@Override
	protected void onResume() {
		super.onResume();

		//打印一下目录结构
		FileUtil.printAll(new File(getApplicationInfo().dataDir));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.d(keyCode);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		LogUtil.d(keyCode);
		return super.onKeyUp(keyCode, event);
	}
}
