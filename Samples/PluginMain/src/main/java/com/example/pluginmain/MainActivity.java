package com.example.pluginmain;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pluginsharelib.SharePOJO;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.manager.PluginCallback;
import com.limpoxe.fairy.manager.PluginManager;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.FileUtil;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

//import android.provider.BaseColumns;
//import com.umeng.analytics.MobclickAgent;

public class MainActivity extends AppCompatActivity {

    /** @hide */ public static final int LOG_ID_MAIN = 0;
    /** @hide */ public static final int LOG_ID_RADIO = 1;
    /** @hide */ public static final int LOG_ID_EVENTS = 2;
    /** @hide */ public static final int LOG_ID_SYSTEM = 3;
    /** @hide */ public static final int LOG_ID_CRASH = 4;

    private BaseAdapter listAdapter;
    private ListView mListView;

    @BindView(R.id.install)
    Button butterTest;

    private ArrayList<PluginDescriptor> plugins = new ArrayList<PluginDescriptor>();
	private Button install;
    private boolean isInstalled = false;
    private Button other;
    private final BroadcastReceiver pluginInstallEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionType = intent.getStringExtra("type");
            String pluginId = intent.getStringExtra("id");
            int code = intent.getIntExtra("code", -1);

            Toast.makeText(MainActivity.this,
                    (pluginId==null?"" : ("插件: " + pluginId + ", ")) + "action = " + actionType + ", " + getErrMsg(code),
                    Toast.LENGTH_SHORT).show();

            refreshListView();
        };
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
		setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

		setTitle("插件列表");

		initView();

        // 监听插件安装 安装新插件后刷新当前页面
        registerReceiver(pluginInstallEvent, new IntentFilter(PluginCallback.ACTION_PLUGIN_CHANGED));

        refreshListView();

        PackageManager manager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);

        Log.e("xx", "launcher intent size =" + (infos==null?"0":infos.size()));
        Log.e("xx", "btnText=" + butterTest.getText().toString());
        Log.e("xx", "stringFromJNI " + CxxTest.stringFromJNI());
        testUseLibray();
        CxxTest.println(LOG_ID_MAIN, Log.ERROR, "MainActivity", "end onCreate ");
    }

    private void testUseLibray() {
        AndroidHttpClient androidHttpClient = AndroidHttpClient.newInstance("test/test", getApplicationContext());
        ClassLoader classloader = androidHttpClient.getClass().getClassLoader();
        androidHttpClient.close();
        Log.e("MainActivity", "testUseLibray, classloader=" + classloader);
    }

	private void initView() {
        mListView = (ListView) findViewById(R.id.list);
		install = (Button) findViewById(R.id.install);
        other = (Button) findViewById(R.id.other);

		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                //MobclickAgent.onEvent(MainActivity.this, "test_0");

                //注意Eventbus不能跨进程，要在宿主和插件之间测试EventBus需要使宿主插件在同一个进程
                EventBus.getDefault().post(new MessageEvent("install_click"));

                if (!isInstalled) {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
						int permissionState = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
						if (permissionState != PackageManager.PERMISSION_GRANTED) {
							if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
								requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10086);
							} else {
								Toast.makeText(MainActivity.this, "6.+系统, 请在设置中授权存储卡读写权限", Toast.LENGTH_SHORT).show();
							}
							return;
						}
					}
                    isInstalled = true;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    }, "t-demo-installer").start();

				} else {
					Toast.makeText(MainActivity.this, "点1次就可以啦！", Toast.LENGTH_LONG).show();
				}
			}
		});

        listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return plugins.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = getLayoutInflater().inflate(R.layout.list_item_plugin, null);
                ImageView icon = (ImageView) view.findViewById(R.id.icon);
                TextView appName = (TextView) view.findViewById(R.id.appName);
                TextView packageName = (TextView) view.findViewById(R.id.packageName);
                TextView isStandard = (TextView) view.findViewById(R.id.is_standard);
                TextView pluginVersion = (TextView) view.findViewById(R.id.plugin_version);
                TextView uninstall = (TextView) view.findViewById(R.id.uninstall);
                TextView detail = (TextView) view.findViewById(R.id.detail);

                final PluginDescriptor pluginDescriptor = plugins.get(position);
                appName.setText(ResourceUtil.getLabel(pluginDescriptor));
                packageName.setText(pluginDescriptor.getPackageName());
                isStandard.setText(pluginDescriptor.isStandalone()?"独立插件":"非独立插件");
                pluginVersion.setText(pluginDescriptor.getVersion());
                icon.setImageDrawable(ResourceUtil.getIcon(pluginDescriptor));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //MobclickAgent.onEvent(MainActivity.this, "test_1");
                        testStartActivity2(pluginDescriptor);
                    }
                });

                detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra("plugin_id", pluginDescriptor.getPackageName());
                        startActivity(intent);
                    }
                });

                uninstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //MobclickAgent.onEvent(MainActivity.this, "test_2");
                        PluginManagerHelper.remove(pluginDescriptor.getPackageName());
                    }
                });

                return view;
            }
        };
        mListView.setAdapter(listAdapter);

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MobclickAgent.onEvent(MainActivity.this, "test_3");
                startActivity(new Intent(MainActivity.this, TestCaseListActivity.class));

                testProvider();
            }
        });
	}


    private void testStartActivity2(PluginDescriptor pluginDescriptor) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pluginDescriptor.getPackageName());
        if (launchIntent != null) {
            //打开插件的Launcher界面
            if (!pluginDescriptor.isStandalone()) {
                //测试向非独立插件传宿主中定义的VO对象
                launchIntent.putExtra("paramVO", new SharePOJO("宿主传过来的测试VO"));
            }
            startActivity(launchIntent);
        } else {
            Toast.makeText(MainActivity.this, "插件"  + pluginDescriptor.getPackageName() + "没有配置Launcher", Toast.LENGTH_SHORT).show();
            //没有找到Launcher，打开插件详情
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("plugin_id", pluginDescriptor.getPackageName());
            startActivity(intent);
        }
    }

	private void copyAndInstall(String name) {
		try {
			InputStream assestInput = getAssets().open(name);
            File file = getExternalFilesDir(null);
            if (file == null) {
                Toast.makeText(MainActivity.this, "ExternalFilesDir not exist", Toast.LENGTH_LONG).show();
                return;
            }
			String dest = file.getAbsolutePath() + "/" + name;
			if (FileUtil.copyFile(assestInput, dest)) {
				PluginManagerHelper.installPlugin(dest);
			} else {
				assestInput = getAssets().open(name);
                file = getCacheDir();
                if (file == null) {
                    Toast.makeText(MainActivity.this, "CacheDir not exist", Toast.LENGTH_LONG).show();
                    return;
                }
				dest = file.getAbsolutePath() + "/" + name;
				if (FileUtil.copyFile(assestInput, dest)) {
					PluginManagerHelper.installPlugin(dest);
				} else {
					Toast.makeText(MainActivity.this, "抽取assets中的Apk失败" + dest, Toast.LENGTH_LONG).show();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == 10086) {
			if (permissions != null && permissions.length > 0
					&& grantResults != null && grantResults.length > 0) {
				if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					install.performClick();
				}
			}
		}
	}

	private void refreshListView() {
        plugins.clear();
        plugins.addAll(PluginManagerHelper.getPlugins());
        listAdapter.notifyDataSetChanged();

        if (plugins.size() > 0) {
            other.setVisibility(View.VISIBLE);
        } else {
            other.setVisibility(View.GONE);
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(pluginInstallEvent);
        EventBus.getDefault().unregister(this);
	};


	private static String getErrMsg(int code) {
		if(code== PluginManagerHelper.SUCCESS) {
			return "安装成功";
		} else if (code == PluginManagerHelper.SRC_FILE_NOT_FOUND) {
			return "失败: 安装文件未找到";
		} else if (code == PluginManagerHelper.COPY_FILE_FAIL) {
			return "失败: 复制安装文件到安装目录失败";
		} else if (code == PluginManagerHelper.SIGNATURES_INVALIDATE) {
			return "失败: 安装文件验证失败";
		} else if (code == PluginManagerHelper.VERIFY_SIGNATURES_FAIL) {
			return "失败: 插件和宿主签名串不匹配";
		} else if (code == PluginManagerHelper.PARSE_MANIFEST_FAIL) {
			return "失败: 插件Manifest文件解析出错";
		} if (code == PluginManagerHelper.FAIL_BECAUSE_SAME_VER_HAS_LOADED) {
			return "失败: 同版本插件已加载,无需安装";
		} else if (code == PluginManagerHelper.MIN_API_NOT_SUPPORTED) {
			return "失败: 当前系统版本过低,不支持此插件";
		} else if (code == PluginManagerHelper.HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN) {
            return "失败: 插件要求的宿主版本和当前宿主版本不匹配";
        }  else if (code == PluginManagerHelper.REMOVE_FAIL_PLUGIN_NOT_EXIST) {
            return "失败: 插件不存在";
        } else if (code == PluginManagerHelper.REMOVE_SUCCESS) {
            return "删除插件成功";
        } else if (code == PluginManagerHelper.REMOVE_FAIL) {
            return "失败: 删除插件失败";
        } else {
			return "失败: 其他 code=" + code;
		}
	}

	//测试在宿主中调用插件的conentProvider
    public static final String AUTHORITY = "com.example.plugintest.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/pluginfirst");
    public static final String MY_FIRST_PLUGIN_NAME = "my_first_plugin_name";

	@Override
	protected void onResume() {
		super.onResume();

		//打印一下目录结构
		//FileUtil.printAll(new File(getApplicationInfo().dataDir));

        //MobclickAgent.onResume(this);
	}

    /**
     * 测试在宿主中调用插件的conentProvider
     */
	private void testProvider() {
        //因为目标在插件中，所以要先判断插件是否已经安装
        boolean isInstalled = PluginManager.isInstalled("com.example.plugintest");
        boolean isRunning = PluginManager.isRunning("com.example.plugintest");
	    if (!isInstalled || !isRunning) {
	        return;
        }

        ContentValues values = new ContentValues();
        values.put(MY_FIRST_PLUGIN_NAME, "test web" + System.currentTimeMillis());
        Uri uri = getContentResolver().insert(CONTENT_URI, values);
        LogUtil.d("insert", "uri=" + uri);

        boolean isSuccess = false;
        Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MY_FIRST_PLUGIN_NAME);
                if (index != -1) {
                    isSuccess = true;
                    String pluginName = cursor.getString(index);
                    Log.d("query", pluginName);
                    Toast.makeText(this, "ContentResolver " + pluginName + " count=" + cursor.getCount(), Toast.LENGTH_LONG).show();
                }
            }
            cursor.close();
        }
        if (!isSuccess) {
            Toast.makeText(this, "ContentResolver 查无数据", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
        Debug.trackHuaweiReceivers();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent event) {
        LogUtil.d("宿主响应了事件 onEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onXXXEvent(MessageEvent event) {
        LogUtil.d("宿主响应了事件 onXXXEvent");
    }
}
