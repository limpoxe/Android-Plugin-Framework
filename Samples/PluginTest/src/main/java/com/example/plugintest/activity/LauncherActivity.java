package com.example.plugintest.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.pluginsharelib.BaseActivity;
import com.example.pluginsharelib.IHostAidlInterface;
import com.example.pluginsharelib.SharePOJO;
import com.example.plugintest.Log;
import com.example.plugintest.R;
import com.example.plugintest.receiver.PluginTestReceiver2;
import com.example.plugintest.service.PluginTestService;
import com.example.plugintest.vo.ParamVO;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackActivity;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;
//import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

//import com.example.plugintest.databinding.PluginLauncherBinding;

public class LauncherActivity extends BaseActivity implements View.OnClickListener {

    //Test ButterKnife
    @BindView(R.id.onClickHellowrld)
    Button butterTest;

    //Test UmengSdk
    Activity fakeThisForUmengSdk;

    private IHostAidlInterface mService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IHostAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.setExtrasClassLoader(ParamVO.class.getClassLoader());
            String msg = intent.getStringExtra("msg");
            ParamVO paramVO = (ParamVO)intent.getSerializableExtra("vo");
            Toast.makeText(context, msg + ", " + paramVO, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //测试databinding
		//PluginLauncherBinding bing =  DataBindingUtil.setContentView(this, R.layout.plugin_launcher);
		//DataBindingTestVO dataBindingTestVO = new DataBindingTestVO("DataBind:打开PluginHellWorld");
		//bing.setTest(dataBindingTestVO);

		setContentView(R.layout.plugin_launcher);
        ButterKnifeCompat.bind(this);

		testLog();

        fakeThisForUmengSdk = fakeActivityForUMengSdk(LauncherActivity.this);

        requestPermission();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("这是插件首屏");
		actionBar.setSubtitle("这是副标题");
		actionBar.setLogo(R.drawable.ic_launcher);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

		findViewById( R.id.onClickHellowrld).setOnClickListener(this);
		findViewById( R.id.onClickPluginNormalFragment).setOnClickListener(this);
		findViewById( R.id.onClickPluginSpecFragment).setOnClickListener(this);
		findViewById( R.id.onClickPluginForDialogActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginForOppoAndVivoActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginNotInManifestActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginFragmentTestActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginSingleTaskActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestOpenPluginActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestTabActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginWebViewActivity).setOnClickListener(this);
		findViewById( R.id.onClickTransparentActivity).setOnClickListener(this);
		findViewById( R.id.onClickDesignActivity).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestReceiver).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestReceiver2).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestService).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestService2).setOnClickListener(this);
		findViewById( R.id.onTestFileProvider).setOnClickListener(this);

        testQueryIntentActivities();
        testAlarm();
        testService();
        testMeta();
        testVersion1();
        testVersion2();
    }

    private void testVersion1() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            LogUtil.v("plugin", pi.versionName, pi.versionCode);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
    }

    private void testVersion2() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(FairyGlobal.getHostApplication().getPackageName(), 0);
            LogUtil.v("host", pi.versionName, pi.versionCode);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
    }

    private void testMeta() {
        try {
            ApplicationInfo application = (ApplicationInfo)getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String hellowMeta = (String)application.metaData.get("abcdef");
            Toast.makeText(this, hellowMeta + "", Toast.LENGTH_SHORT).show();

			hellowMeta = (String)getApplicationInfo().metaData.get("abcdef");
			LogUtil.d("abcdef", hellowMeta);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void testAlarm() {
        registerReceiver(broadcastReceiver, new IntentFilter("ACTION_ALARM_TEST"));

//        Intent intent = new Intent("ACTION_ALARM_TEST");
//        intent.putExtra("msg", "测试Alarm");
//        intent.putExtra("vo", new ParamVO());
//        intent.setExtrasClassLoader(ParamVO.class.getClassLoader());
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5*1000, pendingIntent);
    }

    private void testService() {
        Intent intent = new Intent();
        intent.setAction("com.example.HostService");
        //从 Android 5.0开始 隐式Intent绑定服务的方式已不能使用,所以这里需要设置Service所在服务端的包名
        intent.setPackage("com.example.pluginmain");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void testLog() {
        Log.e("xxx1", "activity_welcome ID= " + R.layout.plugin_launcher);
        Log.e("xxx2", getResources().getResourceEntryName(R.layout.plugin_launcher));
        Log.e("xxx3", getResources().getString(R.string.app_name) + "  " + getPackageManager().getApplicationLabel(getApplicationInfo()));
        Log.e("xxx4", getPackageName() + ", " + getText(R.string.app_name));
        Log.e("xxx5", getResources().getString(android.R.string.httpErrorBadUrl));
        Log.e("xxx6", getResources().getString(getResources().getIdentifier("app_name", "string", "com.example.plugintest")));
        Log.e("xxx7", getResources().getString(getResources().getIdentifier("app_name", "string", getPackageName())));
        Log.e("xxx8", getResources().getString(getResources().getIdentifier("app_name", "string", "com.example.pluginmain")));
        Log.e("xxx9", butterTest.getText());
    }

    private void testNotification() {
        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(com.example.pluginmain.R.drawable.ic_launcher);
        mBuilder.setContentTitle("PluginTest Title").setContentText("PluginTest Content")
                .setTicker("PluginTest Ticker");
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        //mBuilder.setContentIntent()
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        LogUtil.e("NotificationManager.notify");
        mNotificationManager.notify(123, mNotification);
    }

    private void testQueryIntentActivities() {
        PackageManager manager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);

        Log.e("xx", "infos=" + (infos==null?"0":infos.size()));
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 17) {
            boolean isGranted = true;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED) {
                    isGranted = false;
                    requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 10086);
                }
            }
            if (isGranted) {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission") List<CellInfo> list =  telephonyManager.getAllCellInfo();
                if (list != null) {
                    LogUtil.v(list);
                }
            }
        }
    }

	private static void startFragmentInHostActivity(Context context, String targetId) {
		Intent pluginActivity = new Intent();
		pluginActivity.setClassName(context, "com.example.pluginmain.TestFragmentActivity");
		pluginActivity.putExtra("PluginDispatcher.fragmentId", targetId);
		pluginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pluginActivity);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.onClickHellowrld:
				onClickHellowrld(v);
				break;
			case R.id.onClickPluginNormalFragment:
				onClickPluginNormalFragment(v);
				break;
			case R.id.onClickPluginSpecFragment:
				onClickPluginSpecFragment(v);
				break;
			case R.id.onClickPluginForDialogActivity:
				onClickPluginForDialogActivity(v);
				break;
			case R.id.onClickPluginForOppoAndVivoActivity:
				onClickPluginForOppoAndVivoActivity(v);
				break;
			case R.id.onClickPluginNotInManifestActivity:
				onClickPluginNotInManifestActivity(v);
				break;
			case R.id.onClickPluginFragmentTestActivity:
				onClickPluginFragmentTestActivity(v);
				break;
			case R.id.onClickPluginSingleTaskActivity:
				onClickPluginSingleTaskActivity(v);
				break;
			case R.id.onClickPluginTestActivity:
				onClickPluginTestActivity(v);
				break;
			case R.id.onClickPluginTestOpenPluginActivity:
				onClickPluginTestOpenPluginActivity(v);
				break;
			case R.id.onClickPluginTestTabActivity:
				onClickPluginTestTabActivity(v);
				break;
			case R.id.onClickPluginWebViewActivity:
				onClickPluginWebViewActivity(v);
				break;
			case R.id.onClickTransparentActivity:
				onClickTransparentActivity(v);
				break;
			case R.id.onClickDesignActivity:
				onClickDesignActivity(v);
				break;
			case R.id.onClickPluginTestReceiver:
				onClickPluginTestReceiver(v);
				break;
			case R.id.onClickPluginTestReceiver2:
				onClickPluginTestReceiver2(v);
				break;
			case R.id.onClickPluginTestService:
				onClickPluginTestService(v);
				break;
			case R.id.onClickPluginTestService2:
				onClickPluginTestService2(v);
				break;
			case R.id.onTestFileProvider:
				testFileProvider();
				break;
		}
	}

	private void testFileProvider() {
		Intent intent = new Intent("com.android.camera.action.CROP");

		//注意修改为自己设备上真实存在的地址
		File file = new File("/storage/emulated/0/Pictures/Screenshots/1.png");

		if(!file.exists()) {
			Toast.makeText(getApplicationContext(), "图片不存在：" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			return;
		}

		Uri photoURI = FileProvider.getUriForFile(context, "a.b.c.fileprovider", file);

		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setDataAndType(photoURI, "image/*");

		intent.putExtra("crop", "true");
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", false);
		startActivityForResult(intent, 111);
	}

	public void onClickHellowrld(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_4");

        if (PluginManagerHelper.isInstalled("com.example.pluginhelloworld")) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.pluginhelloworld");
            if (intent != null) {
                intent.putExtra("testParam", "testParam");
                startActivity(intent);
            } else {
                Log.e("onClickHellowrld", "No Launcher Intent");
            }
        } else {
            Log.e("onClickHellowrld", "Not Installed");
        }

        if (mService != null) {
            try {
                mService.basicTypes(1,2,true,4f,5d,"hostAIDL");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
	}

	public void onClickPluginNormalFragment(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_5");

        startFragmentInHostActivity(this, "some_id_for_fragment1");
	}

	public void onClickPluginSpecFragment(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_6");

        startFragmentInHostActivity(this, "some_id_for_fragment2");
	}

	public void onClickPluginForDialogActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_7");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginForDialogActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginForOppoAndVivoActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_8");

        //利用Action打开
		Intent intent = new Intent("test.ijk");
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginNotInManifestActivity(View v) {
		//利用scheme打开
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse("testscheme://testhost"));
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);

	}

	public void onClickPluginFragmentTestActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_9");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginFragmentTestActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginSingleTaskActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_10");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginSingleTaskActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);

		show(v);
	}

	public void onClickPluginTestActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_11");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);

	}

	public void onClickPluginTestOpenPluginActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_12");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestOpenPluginActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginTestTabActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_13");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestTabActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginWebViewActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_14");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginWebViewActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickTransparentActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_15");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, TransparentActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickDesignActivity(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_16");

        Intent intent = new Intent(this, DesignActivity.class);
		startActivity(intent);

        testNotification();

//        Intent resultIntnet = new Intent();
//        resultIntnet.setClassName("com.example.pluginmain", "com.example.pluginmain.TestCaseListActivity");
//        startActivityForResult(resultIntnet, 10086);
	}

	public void onClickPluginTestReceiver(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_17");

        //利用Action打开
		Intent intent = new Intent("test.rst2");//两个Receive都配置了这个aciton，这里可以同时唤起两个Receiver
		intent.putExtra("testParam", "testParam");
		sendBroadcast(intent);
	}

	public void onClickPluginTestReceiver2(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_18");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestReceiver2.class.getName());
		intent.putExtra("testParam", "testParam");
		sendBroadcast(intent);
	}

	public void onClickPluginTestService(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_19");

        //利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestService.class.getName());
		intent.putExtra("testParam", "testParam");
		startService(intent);
		//stopService(intent);
	}

	public void onClickPluginTestService2(View v) {
        //MobclickAgent.onEvent(fakeThisForUmengSdk, "test_20");

        //利用Action打开
		Intent intent = new Intent("test.lmn2");
		intent.putExtra("testParam", "testParam");
		startService(intent);
		//stopService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("cc");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
        //MobclickAgent.onResume(fakeThisForUmengSdk);

		testDataApi();
	}

    public void onPause() {
        super.onPause();
        //MobclickAgent.onPause(fakeThisForUmengSdk);
    }

	public static void show(View rootView) {

		View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout.plugin_notification, null);
		PopupWindow window = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		window.setAnimationStyle(R.style.PopupAnimation1);
		window.setOutsideTouchable(true);
		window.setFocusable(true);
		window.setBackgroundDrawable(new BitmapDrawable());
		window.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}

	private void testDataApi() {

		SharedPreferences sp = getSharedPreferences("aaa", 0);
		sp.edit().putString("xyz", "123").commit();
		File f = getDir("bbb", 0);
		Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		f = getFilesDir();
		Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		if (Build.VERSION.SDK_INT >= 21) {
			f = getNoBackupFilesDir();
			Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());
		}

		f = getCacheDir();
		Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		if (Build.VERSION.SDK_INT >= 21) {
			f = getCodeCacheDir();
		}
		Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		SQLiteDatabase db = openOrCreateDatabase("ccc", 0, null);
		try {
			String sql = "create table IF NOT EXISTS  userDb (_id integer primary key autoincrement, column_one text not null);";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}

		f = getDatabasePath("ccc");
		Log.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		String[] list = databaseList();

		try {
			FileOutputStream fo = openFileOutput("ddd", 0);
			fo.write(122);
			fo.flush();
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d(getFileStreamPath("eee").getAbsolutePath());

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(keyCode);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(keyCode);
		return super.onKeyUp(keyCode, event);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtil.d("onActivityResult", requestCode, resultCode);

        Toast.makeText(getApplicationContext(), "onActivityResult", Toast.LENGTH_LONG).show();

//        if (data != null) {
//            LogUtil.d("onActivityResult data", data.getStringExtra("ret"));
//        }
    }

    public static Activity fakeActivityForUMengSdk(Activity activity) {
        //getApplication();
        //getApplicationContext();
        //getPackageName();
        //getLocalClassName();
        final String className = activity.getClass().getSimpleName();
        Activity fakeActivity = new Activity() {
            @Override
            public Context getApplicationContext() {
                return FairyGlobal.getHostApplication().getApplicationContext();
            }

            @Override
            public String getPackageName() {
                return FairyGlobal.getHostApplication().getPackageName();
            }

            public String getLocalClassName() {
                return className;
            }
        };
        new HackActivity(fakeActivity).setApplication(FairyGlobal.getHostApplication());
        return fakeActivity;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            unbindService(connection);
        }
        unregisterReceiver(broadcastReceiver);
    }
}