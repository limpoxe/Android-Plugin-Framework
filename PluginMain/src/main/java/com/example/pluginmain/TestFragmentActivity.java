package com.example.pluginmain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.plugin.core.PluginLoader;
import com.plugin.core.annotation.PluginContainer;

/**
 * 一个非常普通的FragmentActivty， 用来展示一个来自插件中的fragment。
 * 这里需要通过注解@FragmentContainer来通知插件框架,此activity要展示
 * 的fragment来自那个插件，从而提前更换当前Activity的Context为插件Context
 *
 * @author cailiming
 * 
 */
@PluginContainer(pluginId = "com.example.plugintest")
public class TestFragmentActivity extends AppCompatActivity {

	public static final String FRAGMENT_ID_IN_PLUGIN = "PluginDispatcher.fragmentId";
	private static final String LOG_TAG = TestFragmentActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_activity);

		loadPluginFragment();
	}

	private void loadPluginFragment() {
		try {
			String classId = getIntent().getStringExtra(FRAGMENT_ID_IN_PLUGIN);
			if (classId == null) {
				Toast.makeText(this, "缺少参数:PluginDispatcher.fragmentId", Toast.LENGTH_SHORT).show();
				return;
			}
			Log.d(LOG_TAG, "loadPluginFragment, classId is " + classId);
			@SuppressWarnings("rawtypes")
			Class clazz = PluginLoader.loadPluginFragmentClassById(classId);
			Fragment fragment = (Fragment) clazz.newInstance();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, fragment).commit();
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
