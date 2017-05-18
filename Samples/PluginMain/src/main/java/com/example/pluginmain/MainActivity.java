package com.example.pluginmain;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import com.example.pluginsharelib.SharePOJO;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.manager.PluginCallback;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.FileUtil;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

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

        Log.e("xx", "infos=" + (infos==null?"0":infos.size()));
        Log.e("xx", butterTest.getText().toString());

    }

	private void initView() {
        mListView = (ListView) findViewById(R.id.list);
		install = (Button) findViewById(R.id.install);
        other = (Button) findViewById(R.id.other);

		install.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
                    }).start();

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

                final PluginDescriptor pluginDescriptor = plugins.get(position);
                appName.setText(ResourceUtil.getLabel(pluginDescriptor));
                packageName.setText(pluginDescriptor.getPackageName());
                isStandard.setText(pluginDescriptor.isStandalone()?"独立插件":"非独立插件");
                pluginVersion.setText(pluginDescriptor.getVersion());
                icon.setImageDrawable(ResourceUtil.getIcon(pluginDescriptor));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        testStartActivity2(pluginDescriptor);
                    }
                });

                uninstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PluginManagerHelper.remove(pluginDescriptor.getPackageName());
                        refreshListView();
                    }
                });

                return view;
            }
        };
        mListView.setAdapter(listAdapter);

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestCaseListActivity.class));
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
			String dest = getExternalFilesDir(null).getAbsolutePath() + "/" + name;
			if (FileUtil.copyFile(assestInput, dest)) {
				PluginManagerHelper.installPlugin(dest);
			} else {
				assestInput = getAssets().open(name);
				dest = getCacheDir().getAbsolutePath() + "/" + name;
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
	};


	private static String getErrMsg(int code) {
		if(code== PluginManagerHelper.SUCCESS) {
			return "成功";
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
		} else if (code == PluginManagerHelper.PLUGIN_NOT_EXIST) {
			return "失败: 插件不存在";
		} else if (code == PluginManagerHelper.REMOVE_FAIL) {
			return "失败: 删除插件失败";
		} else if (code == PluginManagerHelper.HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN) {
            return "失败: 插件要求的宿主版本和当前宿主版本不匹配";
        } else {
			return "失败: 其他 code=" + code;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//打印一下目录结构
		FileUtil.printAll(new File(getApplicationInfo().dataDir));
        MobclickAgent.onResume(this);
	}

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
