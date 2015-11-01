package com.example.plugintest.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.hellojni.HelloJni;
import com.example.plugintest.R;
import com.example.plugintest.provider.PluginDbTables;
import com.plugin.core.PluginIntentResolver;
import com.plugin.core.PluginLoader;
import com.plugin.core.PluginRemoteViewHelper;
import com.plugin.util.FileUtil;
import com.plugin.util.LogUtil;

import java.io.File;
import java.io.InputStream;

public class PluginWebViewActivity extends Activity implements OnClickListener {
	WebView web;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_webview);

		Button bn = (Button) findViewById(R.id.load);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_insert);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_read);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_so);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_assert);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_notification);
		bn.setOnClickListener(this);

		web = (WebView) findViewById(R.id.webview);
		setUpWebViewSetting();
		setClient();

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.load) {
			web.loadUrl("http://www.baidu.com/");
		} else if (v.getId() == R.id.db_insert) {

			//插件ContentProvider是在插件首次被唤起时安装的, 属于动态安装。
			//因此需要在插件被唤起后才可以使用相应的ContentProvider
			//若要静态安装，需要更改PluginLoader的安装策略～

			ContentValues values = new ContentValues();
			values.put(PluginDbTables.PluginFirstTable.MY_FIRST_PLUGIN_NAME, "test web" + System.currentTimeMillis());
			getContentResolver().insert(PluginDbTables.PluginFirstTable.CONTENT_URI, values);


		} else if (v.getId() == R.id.db_read) {
			Cursor cursor = getContentResolver().query(PluginDbTables.PluginFirstTable.CONTENT_URI, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(PluginDbTables.PluginFirstTable.MY_FIRST_PLUGIN_NAME);
					if (index != -1) {
						String pluginName = cursor.getString(index);
						LogUtil.d(pluginName);
						Toast.makeText(this, "ContentResolver " + pluginName + " count=" + cursor.getCount(), Toast.LENGTH_LONG).show();
					}
				}
				cursor.close();
			}
		} else if (v.getId() == R.id.db_so) {

			Toast.makeText(this, "Test Jni so libaray 4 + 7 = "+  HelloJni.calculate(4, 7), Toast.LENGTH_LONG).show();

		} else if (v.getId() == R.id.db_assert) {

			testReadAssert();
		} else if (v.getId() == R.id.db_notification) {

			testNotification();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void testNotification() {

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);

		Intent intent =  new Intent();

		//唤起宿主Activity
		//intent.setClassName(getPackageName(), "com.example.pluginmain.PluginDetailActivity");
		//唤起插件Activity
		intent.setClassName(getPackageName(), PluginTestActivity.class.getName());
		//还可以支持唤起service、receiver等等。

		intent.putExtra("param1", "这是来自通知栏的参数");
		intent = PluginIntentResolver.resolveNotificationIntent(intent);

		PendingIntent contentIndent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIndent)
				.setSmallIcon(com.example.pluginsharelib.R.drawable.ic_launcher)//设置状态栏里面的图标（小图标） 　　　　　　　　　　　　　　　　　　　　.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.i5))//下拉下拉列表里面的图标（大图标） 　　　　　　　.setTicker("this is bitch!") //设置状态栏的显示的信息
				.setWhen(System.currentTimeMillis())//设置时间发生时间
				.setAutoCancel(true)//设置可以清除
				.setContentTitle("来自插件ContentTitle")//设置下拉列表里的标题
				.setDefaults(Notification.DEFAULT_SOUND)//设置为默认的声音
				.setContentText("来自插件ContentText");//设置上下文内容

		if (Build.VERSION.SDK_INT >=21) {
			RemoteViews remoteViews = PluginRemoteViewHelper.createRemoteViews(
					R.layout.plugin_notification,
					new File(Environment.getExternalStorageDirectory(), "tempNotificationRes.apk").getAbsolutePath(),
					PluginLoader.getPluginDescriptorByClassName(PluginWebViewActivity.class.getName()).getPackageName());
			builder.setContent(remoteViews);
		}

		Notification notification = builder.getNotification();
		notificationManager.notify(R.drawable.ic_launcher, notification);

	}

	private void testReadAssert() {
		try {
			InputStream assestInput = getAssets().open("test.json");
			String text = FileUtil.streamToString(assestInput);
			Toast.makeText(this, "read assets from plugin" + text, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setUpWebViewSetting() {
		WebSettings webSettings = web.getSettings();

		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);// 根据cache-control决定是否从网络上取数据
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);// 显示放大缩小
		webSettings.setJavaScriptEnabled(true);
		// webSettings.setPluginsEnabled(true);
		webSettings.setPluginState(PluginState.ON);
		webSettings.setUserAgentString(webSettings.getUserAgentString());
		webSettings.setDomStorageEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCachePath(getCacheDir().getPath());
		webSettings.setUseWideViewPort(true);// 影响默认满屏和双击缩放
		webSettings.setLoadWithOverviewMode(true);// 影响默认满屏和手势缩放

	}

	private void setClient() {

		web.setWebChromeClient(new WebChromeClient() {
		});

		// 如果要自动唤起自定义的scheme，不能设置WebViewClient，
		// 否则，需要在shouldOverrideUrlLoading中自行处理自定义scheme
		// webView.setWebViewClient();
		web.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

		});
	}
}
