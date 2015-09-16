package com.example.plugintest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugintest.R;
import com.plugin.util.LogUtil;

/**
 * @author cailiming
 * 
 */
public class PluginForDialogActivity extends Activity {

	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("测试插件中的Dailog");

		View scrollview = getLayoutInflater().inflate(R.layout.plugin_layout, null);
		setContentView(scrollview);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Title");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setView(getLayoutInflater().inflate(R.layout.plugin_layout, null));
		builder.setPositiveButton("Positive", null);
		builder.setNegativeButton("Negative", null);
		dialog = builder.create();

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				Toast.makeText(PluginForDialogActivity.this, "dismiss", Toast.LENGTH_LONG).show();
			}
		});
		dialog.show();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}
}
