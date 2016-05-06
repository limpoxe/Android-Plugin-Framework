package com.example.plugintest.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import com.example.pluginsharelib.SharePOJO;
import com.example.plugintest.R;
import com.example.plugintest.receiver.PluginTestReceiver2;
import com.example.plugintest.service.PluginTestService;
import com.plugin.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_launcher);

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
		findViewById( R.id.onClickPluginTestReceiver).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestReceiver2).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestService).setOnClickListener(this);
		findViewById( R.id.onClickPluginTestService2).setOnClickListener(this);
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
		}
	}

	public void onClickHellowrld(View v) {
		Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.pluginhelloworld");
		intent.putExtra("testParam", "testParam");
		startActivity(intent);
	}

	public void onClickPluginNormalFragment(View v) {
		startFragmentInHostActivity(this, "some_id_for_fragment1");
	}

	public void onClickPluginSpecFragment(View v) {
		startFragmentInHostActivity(this, "some_id_for_fragment2");
	}

	public void onClickPluginForDialogActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginForDialogActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginForOppoAndVivoActivity(View v) {
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
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginFragmentTestActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginSingleTaskActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginSingleTaskActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginTestActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);

	}

	public void onClickPluginTestOpenPluginActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestOpenPluginActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginTestTabActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestTabActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginWebViewActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginWebViewActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickTransparentActivity(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, TransparentActivity.class.getName());
		intent.putExtra("testParam", "testParam");
		intent.putExtra("paramVO", new SharePOJO("测试VO"));
		startActivity(intent);
	}

	public void onClickPluginTestReceiver(View v) {
		//利用Action打开
		Intent intent = new Intent("test.rst2");//两个Receive都配置了这个aciton，这里可以同时唤起两个Receiver
		intent.putExtra("testParam", "testParam");
		sendBroadcast(intent);
	}

	public void onClickPluginTestReceiver2(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestReceiver2.class.getName());
		intent.putExtra("testParam", "testParam");
		sendBroadcast(intent);
	}

	public void onClickPluginTestService(View v) {
		//利用className打开
		Intent intent = new Intent();
		intent.setClassName(this, PluginTestService.class.getName());
		intent.putExtra("testParam", "testParam");
		startService(intent);
		//stopService(intent);
	}

	public void onClickPluginTestService2(View v) {
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

		testDataApi();
	}

	private void testDataApi() {

		SharedPreferences sp = getSharedPreferences("aaa", 0);
		sp.edit().putString("xyz", "123").commit();
		File f = getDir("bbb", 0);
		LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		f = getFilesDir();
		LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		if (Build.VERSION.SDK_INT >= 21) {
			f = getNoBackupFilesDir();
			LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());
		}

		f = getCacheDir();
		LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		if (Build.VERSION.SDK_INT >= 21) {
			f = getCodeCacheDir();
		}
		LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

		SQLiteDatabase db = openOrCreateDatabase("ccc", 0, null);
		try {
			String sql = "create table IF NOT EXISTS  userDb (_id integer primary key autoincrement, column_one text not null);";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}

		f = getDatabasePath("ccc");
		LogUtil.d(f.getAbsoluteFile(), f.exists(), f.canRead(), f.canWrite());

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

		LogUtil.d(getFileStreamPath("eee").getAbsolutePath());

	}
}