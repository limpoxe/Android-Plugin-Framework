package com.example.pluginhelloworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hellojni.HelloJni;

/**
 * 独立插件测试demo
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("xxx1", "activity_welcome ID= " + R.layout.activity_welcome);
        Log.e("xxx2", getResources().getResourceEntryName(R.layout.activity_welcome));
        Log.e("xxx3", getResources().getString(R.string.app_name));
        Log.e("xxx4", getPackageName() + ", " + getText(R.string.app_name));
        Log.e("xxx5", getResources().getString(android.R.string.httpErrorBadUrl));
        Log.e("xxx6", getResources().getString(getResources().getIdentifier("app_name", "string", "com.example.pluginhelloworld")));
        Log.e("xxx7", getResources().getString(getResources().getIdentifier("app_name", "string", getPackageName())));

        setContentView(R.layout.activity_welcome);

        findViewById(R.id.test_s_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(WelcomeActivity.this, "测试JNI：3 + 4 = " +  HelloJni.calculate(3, 4), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.test_switch_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
        });

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
