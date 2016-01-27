package com.example.pluginhelloworld;

import android.app.Activity;
import android.os.Bundle;

/**
 * 独立插件测试demo
 */
public class TransparentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);
    }
}
