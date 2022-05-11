package com.example.pluginhelloworld;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 独立插件测试demo
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.example.pluginhelloworld", PackageManager.GET_META_DATA);
            String hellowMeta = (String)info.metaData.get("hello_meta");
            Toast.makeText(this, hellowMeta, Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.e("xxx1", "activity_welcome ID= " + R.layout.activity_welcome);
        Log.e("xxx2", getResources().getResourceEntryName(R.layout.activity_welcome));
        Log.e("xxx3", getResources().getString(R.string.app_name));
        Log.e("xxx4", getPackageName() + ", " + getText(R.string.app_name));
        Log.e("xxx5", getResources().getString(android.R.string.httpErrorBadUrl));
        Log.e("xxx6", getResources().getString(getResources().getIdentifier("app_name", "string", "com.example.pluginhelloworld")));
        Log.e("xxx7", getResources().getString(getResources().getIdentifier("app_name", "string", getPackageName())));

        findViewById(R.id.test_dexmaker_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Stock stock = ProxyBuilder.forClass(Stock.class)
                            .dexCache(getDir("dexmaker", Context.MODE_PRIVATE))
                            .handler(new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    Log.d("ProxyBuilder", "before " + method.getName());
                                    Object result = ProxyBuilder.callSuper(proxy, method, args);
                                    Log.d("ProxyBuilder", "after " + method.getName());
                                    return result;
                                }
                            })
                            .build();
                    stock.testProxyMethod("test proxy method");
                    Log.d("WelcomeActivity", "Real Stock Class : " + stock.getClass().getName());
                    Log.d("WelcomeActivity", "Real Stock ClassLoader" + stock.getClass().getClassLoader());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.test_s_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFileProvider();
                Toast.makeText(WelcomeActivity.this, "测试JNI：3 + 4 = " + HelloJni.calculate(3, 4), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.test_switch_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.test_transparent_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, TransparentActivity.class));
            }
        });

        findViewById(R.id.test_installapk_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new File("/sdcard/Download/xx.apk").exists()) {
                    testInstallApk(WelcomeActivity.this, "/sdcard/Download/xx.apk");
                } else {
                    Toast.makeText(WelcomeActivity.this, "apk file not exits!", Toast.LENGTH_LONG).show();
                }
            }
        });

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/local_page_1.html");

        Intent intent = new Intent("test.thirdparty.open");
        sendBroadcast(intent);
    }


    private static void testInstallApk(Context context, String apkPath) {
        if (context == null || TextUtils.isEmpty(apkPath)) {
            return;
        }
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(context, "x.y.z.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    private void testFileProvider() {
        Intent intent = new Intent("com.android.camera.action.CROP");

        //注意修改为自己设备上真实存在的地址
        File srcfile = new File("/storage/emulated/0/Pictures/Screenshots/1.png");

        if(!srcfile.exists()) {
            //oast.makeText(getApplicationContext(), "图片不存在：" + srcfile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(this, "x.y.z.fileprovider", srcfile);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(photoURI, "image/*");

        intent.putExtra("crop", "true");
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        File output = new File("/storage/emulated/0/Pictures/Screenshots/", System.currentTimeMillis() + "_crop.png");
        output.getParentFile().mkdirs();
        output.delete();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

        startActivityForResult(intent, 111);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
