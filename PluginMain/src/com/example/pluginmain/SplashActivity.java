package com.example.pluginmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.plugin.core.PluginLoader;

public class SplashActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
	}

	public void open(View view) {

		PluginLoader.initLoader(getApplication());

		startActivity(new Intent(this, MainActivity.class));
	}

}
