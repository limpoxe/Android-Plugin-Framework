package com.example.plugintest.activity;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.plugintest.R;
import com.example.plugintest.hellojni.HelloJni;
import com.example.plugintest.provider.PluginDbTables;
import com.example.plugintestbase.ILoginService;
import com.example.plugintestbase.LoginVO;
import com.limpoxe.fairy.manager.PluginManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings("ALL")
public class PluginWebViewActivity extends AppCompatActivity implements OnClickListener {
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
		bn = (Button) findViewById(R.id.weixin);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.hellow);
		bn.setOnClickListener(this);

		web = (WebView) findViewById(R.id.webview);
		setUpWebViewSetting();
		setClient();

		if (PluginManager.isInstalled("com.example.plugintestbase")) {
			//java.lang.NoClassDefFoundError:
			//ILoginService这个类是由另外一个插件PluginBase提供，发生NoClassDefFoundError说明另外一个插件未安装或者是在安装此插件之后安装的
			ILoginService login = (ILoginService) getSystemService("login_service");
			if (login != null) {
				LoginVO vo = login.login("admin", "123456");
				Toast.makeText(this, vo.getUsername() + ":" + vo.getPassword(), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "ILoginService == null", Toast.LENGTH_SHORT).show();
			}
		}

		try {
			String currentPackageName = getPackageManager().getActivityInfo(new ComponentName(this.getPackageName(), this.getClass().getName()), 0).packageName;
			Toast.makeText(this, "测试PackageManager查询插件信息" + currentPackageName, Toast.LENGTH_SHORT).show();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		web.addJavascriptInterface(this, "test");

		web.loadUrl("file:///android_asset/local_web_test.html");

	}

	@JavascriptInterface
    public void onclick() {
        Toast.makeText(this, "test js onclick", Toast.LENGTH_LONG).show();
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

			Toast.makeText(this, "ContentResolver insert test web", Toast.LENGTH_LONG).show();

		} else if (v.getId() == R.id.db_read) {
			boolean isSuccess = false;
			Cursor cursor = getContentResolver().query(PluginDbTables.PluginFirstTable.CONTENT_URI, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(PluginDbTables.PluginFirstTable.MY_FIRST_PLUGIN_NAME);
					if (index != -1) {
						isSuccess = true;
						String pluginName = cursor.getString(index);
						Log.d("xx", pluginName);
						Toast.makeText(this, "ContentResolver " + pluginName + " count=" + cursor.getCount(), Toast.LENGTH_LONG).show();
					}
				}
				cursor.close();
			}
			if (!isSuccess) {
				Toast.makeText(this, "ContentResolver 查无数据", Toast.LENGTH_LONG).show();
			}
		} else if (v.getId() == R.id.db_so) {

			Toast.makeText(this, "Test Jni so libaray 4 + 7 = "+  HelloJni.calculate(4, 7), Toast.LENGTH_LONG).show();

		} else if (v.getId() == R.id.db_assert) {

			testReadAssert();
		} else if (v.getId() == R.id.db_notification) {

			testNotification();
		} else if (v.getId() == R.id.weixin) {

			//通过packageManager查询其他插件信息并打开,
			// 微信插件中没有配置launcher，所以这里假定用字符串“Send”来匹配
			PackageManager packageManager = getPackageManager();
			try {
				PackageInfo info = packageManager.getPackageInfo("com.example.wxsdklibrary", PackageManager.GET_ACTIVITIES);

				for(ActivityInfo activityInfo:info.activities) {
					if (activityInfo.name.contains("Send")) {
						Intent intent = new Intent();
						intent.setClassName(activityInfo.packageName, activityInfo.name);
						startActivity(intent);
						return;
					}
				}
				Toast.makeText(this, "TargetNotFound", Toast.LENGTH_SHORT).show();
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(this, "NameNotFoundException", Toast.LENGTH_SHORT).show();
			}

		} else if (v.getId() == R.id.hellow) {
			//通过packageManager查询其他插件信息并打开
			PackageManager packageManager = getPackageManager();
			Intent intent = packageManager.getLaunchIntentForPackage("com.example.pluginhelloworld");
			startActivity(intent);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void testNotification() {

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = null;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("111", "CN111", NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(channel);
			builder = new NotificationCompat.Builder(this, "111");
		} else {
			builder = new NotificationCompat.Builder(this);
		}

		Intent intent =  new Intent();

		//唤起宿主Activity
		//intent.setClassName(getPackageName(), "com.example.pluginmain.PluginDetailActivity");
		//唤起插件Activity
		intent.setClassName(getPackageName(), PluginTestActivity.class.getName());
		//还可以支持唤起service、receiver等等。

		intent.putExtra("param1", "这是来自通知栏的参数");

		PendingIntent contentIndent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		builder.setContentIntent(contentIndent)
				//icon只能使用宿主的资源
				.setSmallIcon(com.example.pluginsharelib.R.drawable.ic_launcher)//设置状态栏里面的图标（小图标） 　　　　　　　　　　　　　　　　　　　　
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.i5))//下拉下拉列表里面的图标（大图标） 　　　　　　　
                //.setTicker("this is bitch!") //设置状态栏的显示的信息
				.setWhen(System.currentTimeMillis())//设置时间发生时间
				.setAutoCancel(true)//设置可以清除
				.setContentTitle("来自插件ContentTitle")//设置下拉列表里的标题
				.setDefaults(Notification.DEFAULT_SOUND)//设置为默认的声音
				.setContentText("来自插件ContentText");//设置上下文内容

		if (Build.VERSION.SDK_INT >=21 && Build.VERSION.SDK_INT <26) {
			if (!"Xiaomi".equals(Build.MANUFACTURER)) {
				//测试通知栏携带插件布局资源文件
				builder.setContent(new RemoteViews(getPackageName(), R.layout.plugin_notification));
			}
		}

		Notification notification = builder.getNotification();
		notificationManager.notify(R.drawable.ic_launcher, notification);

	}

	private void testReadAssert() {
		try {
			InputStream assestInput = getAssets().open("test.json");
			String text = streamToString(assestInput);
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
		//webSettings.setAppCacheEnabled(true);
		//webSettings.setAppCachePath(getCacheDir().getPath());
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

	private static String streamToString(InputStream input) throws IOException {

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
