package com.example.plugintest.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.hellojni.HelloJni;
import com.example.plugintest.R;
import com.example.plugintest.provider.PluginDbTables;
import com.plugin.util.LogUtil;

public class PluginWebViewActivity extends Activity implements OnClickListener {
	WebView web;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_webview);

		Button bn = (Button) findViewById(R.id.btn);
		bn.setOnClickListener(this);

		bn = (Button) findViewById(R.id.db_insert);
		bn.setOnClickListener(this);
		bn = (Button) findViewById(R.id.db_read);
		bn.setOnClickListener(this);

		web = (WebView) findViewById(R.id.webview);
		setUpWebViewSetting();
		setClient();

		Toast.makeText(this, "Test Jni so libaray 4 + 7 = "+  HelloJni.calculate(4, 7), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn) {
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
