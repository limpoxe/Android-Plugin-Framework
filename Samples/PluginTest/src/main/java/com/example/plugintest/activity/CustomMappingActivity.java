package com.example.plugintest.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.example.plugintest.R;
import com.limpoxe.fairy.util.LogUtil;

public class CustomMappingActivity extends Activity {

    String text = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_cust_stub_activity);

        LogUtil.d("onCreate");
        text = text + "onCreate ";

        ((TextView)findViewById(R.id.textview)).setText(text);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.d("onConfigurationChanged");

        text = text + "onConfigurationChanged ";

        ((TextView)findViewById(R.id.textview)).setText(text);
    }
}
