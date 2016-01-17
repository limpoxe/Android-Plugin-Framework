package com.example.pluginmain;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.plugin.core.annotation.ComponentContainer;

/**
 * 控件级插件和主题换肤功能不能共存，控件级插件功能默认是关闭的
 * 添加这个注解@ComponentContainer是为了通知插件框架在当前Activity中启用控件级插件
 */
@ComponentContainer
public class TestViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//这个layout里面嵌入了一个来自插件的自定义控件
		setContentView(R.layout.view_activity);

		setTitle("控件级插件");

		initView();
	}

	private void initView() {

		View view = findViewById(R.id.plugin_view);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(TestViewActivity.this, "这是插件定义的控件", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
