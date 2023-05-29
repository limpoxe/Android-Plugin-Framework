package com.example.pluginmain;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.pluginsharelib.SharePOJO;
import com.example.pluginsharelib.ShareService;
import com.example.plugintest.IMyAidlInterface;
import com.limpoxe.fairy.core.localservice.LocalServiceManager;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;

public class TestCaseListActivity extends AppCompatActivity implements View.OnClickListener {
    private ServiceConnection scn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.testcase_list_activity);

        findViewById(R.id.skin).setOnClickListener(this);
        findViewById(R.id.view).setOnClickListener(this);
        findViewById(R.id.local).setOnClickListener(this);
        findViewById(R.id.aidl).setOnClickListener(this);
        findViewById(R.id.tab).setOnClickListener(this);
        findViewById(R.id.popup_window).setOnClickListener(this);
        findViewById(R.id.startService).setOnClickListener(this);
        findViewById(R.id.sendbroadcast).setOnClickListener(this);
        findViewById(R.id.notification).setOnClickListener(this);
        findViewById(R.id.startActivity).setOnClickListener(this);
        findViewById(R.id.startWebActivity).setOnClickListener(this);
        findViewById(R.id.loadWeb).setOnClickListener(this);

        WebView wb = ((WebView)findViewById(R.id.webview));
        setUpWebViewSetting(wb);
        setClient(wb);

        wb.loadUrl("file:///android_asset/host_localweb_test.html");

        if (!PluginManagerHelper.isInstalled("com.example.plugintest")) {
            Toast.makeText(this, "插件未安装:com.example.plugintest", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setUpWebViewSetting(WebView web) {
        WebSettings webSettings = web.getSettings();

        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);// 根据cache-control决定是否从网络上取数据
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);// 显示放大缩小
        webSettings.setJavaScriptEnabled(true);
        // webSettings.setPluginsEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setUserAgentString(webSettings.getUserAgentString());
        webSettings.setDomStorageEnabled(true);
        //webSettings.setAppCacheEnabled(true);
        //webSettings.setAppCachePath(getCacheDir().getPath());
        webSettings.setUseWideViewPort(true);// 影响默认满屏和双击缩放
        webSettings.setLoadWithOverviewMode(true);// 影响默认满屏和手势缩放

    }

    private void setClient(WebView web) {

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

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.skin) {
            Intent intent = new Intent(this, TestSkinActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.view) {
            Intent intent = new Intent(this, TestViewActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.local) {
            ShareService ss = (ShareService) LocalServiceManager.getService("share_service");
            if (ss != null) {
                SharePOJO pojo = ss.doSomething("测试跨进程localservice");
                if (pojo != null) {
                    Toast.makeText(this, pojo.name, Toast.LENGTH_LONG).show();
                }
            }
        } else if (viewId == R.id.aidl) {
            if (scn == null) {
                scn = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
                        try {
                            iMyAidlInterface.basicTypes(1, 2L, true, 0.1f, 0.01d, "测试插件AIDL");
                            Toast.makeText(TestCaseListActivity.this, "onServiceConnected", Toast.LENGTH_LONG).show();
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

        } else if (viewId == R.id.tab) {
            Intent intent = new Intent(this, TestTabActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.popup_window) {
            testPopupWindow(v);
        } else if (viewId == R.id.startService) {
            testStartService();
        } else if (viewId == R.id.sendbroadcast) {
            testSendBroadcast();
        } else if (viewId == R.id.notification) {
            testNotification();
        } else if (viewId == R.id.startActivity) {
            testStartActivity1();
        } else if (viewId == R.id.loadWeb) {
            testLoadWeb();
        } else if (viewId == R.id.startWebActivity) {
            testLoadWeb2();
        }

    }

    private static void testPopupWindow(View rootView) {

        View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout.pop_test, null);
        PopupWindow window = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setAnimationStyle(R.style.PopupAnimation);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(new BitmapDrawable());
        window.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    private void testStartService() {
        //测试通过宿主service唤起插件service
        startService(new Intent(this, MainService.class));
    }

    private void testSendBroadcast() {
        //测试利用Action打开在宿主中唤起插件receiver
        Intent intent = new Intent("test.rst2");//两个Receive都配置了这个aciton，这里可以同时唤起两个Receiver
        intent.putExtra("testParam", "testParam");
        sendBroadcast(intent);
    }

    private void testNotification() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("111", "CN111", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder = new NotificationCompat.Builder(this, "111");
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }

        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("插件框架Title").setContentText("插件框架Content")
                .setTicker("插件框架Ticker");
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        //mBuilder.setContentIntent()
        LogUtil.e("NotificationManager.notify");
        mNotificationManager.notify(456, mNotification);
    }

    private void testStartActivity1() {
        //也可以直接构造Intent，指定打开插件中的某个Activity
        Intent intent = new Intent("test.abc");
        startActivity(intent);
    }

    private void testLoadWeb() {
        ((WebView)findViewById(R.id.webview)).loadUrl("http://www.baidu.com/");
    }

    private void testLoadWeb2() {
        startActivity(new Intent(this, WebActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scn != null) {
            unbindService(scn);
            scn = null;
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("ret", "宿主反馈OK");
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.trackHuaweiReceivers();
    }
}
