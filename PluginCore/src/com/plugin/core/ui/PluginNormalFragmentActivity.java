package com.plugin.core.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.plugin.core.PluginLoader;
import com.plugin.util.FragmentHelper;
import com.plugin.util.LogUtil;

/**
 * 一个非常普通的FragmentActivty， 用来展示一个来自插件中的fragment。
 * 
 * @author cailiming
 * 
 */
public class PluginNormalFragmentActivity extends FragmentActivity {

	private static final String LOG_TAG = PluginNormalFragmentActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FrameLayout root = new FrameLayout(this);
		setContentView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		root.setId(android.R.id.primary);

		loadPluginFragment();
	}

	private void loadPluginFragment() {
		try {
			String classId = getIntent().getStringExtra(FragmentHelper.FRAGMENT_ID_IN_PLUGIN);
			LogUtil.d(LOG_TAG, "loadPluginFragment, classId is " + classId);
			@SuppressWarnings("rawtypes")
			Class clazz = PluginLoader.loadPluginFragmentClassById(classId);
			Fragment fragment = (Fragment) clazz.newInstance();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.primary, fragment).commit();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
