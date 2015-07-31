package com.plugin.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.plugin.core.PluginContextTheme;
import com.plugin.core.PluginDispatcher;
import com.plugin.core.PluginLoader;
import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

/**
 * activity代理, 不建议使用代理模式 建议使用stub模式
 * @author cailiming
 */
@Deprecated
public class PluginProxyActivity extends Activity {

	private Activity activity;

	private Context mOrignalContext;
	private int mOrignalThemeResourceId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context pctx = findPluginContext();
		bindPluginContext(pctx);

		super.onCreate(savedInstanceState);

		FrameLayout root = new FrameLayout(this);
		setContentView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		root.setId(android.R.id.primary);

		loadPluginActivity();

		attach();

		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onCreate", new Class[] { Bundle.class },
				new Object[] { savedInstanceState });
	}
   
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onPostCreate", new Class[] { Bundle.class },
				new Object[] { savedInstanceState });
    }
	
	private Context findPluginContext() {
		String classId = getIntent().getStringExtra(PluginDispatcher.ACTIVITY_ID_IN_PLUGIN);
		LogUtil.d("findPluginContext ", classId);
		@SuppressWarnings("rawtypes")
		Class clazz = PluginLoader.loadPluginClassById(classId);
		if (clazz != null) {
			return PluginLoader.getDefaultPluginContext(clazz);
		}
		return null;
	}

	private void bindPluginContext(Context pctx) {
		if (pctx != null) {
			super.attachBaseContext(new PluginContextTheme(mOrignalContext, pctx.getResources(), pctx.getClassLoader()));
			if (mOrignalThemeResourceId != 0) {
				super.setTheme(mOrignalThemeResourceId);
			}
		} else {
			super.attachBaseContext(mOrignalContext);
		}
	}

	@Override
	public void setTheme(int resid) {
		if (getBaseContext() == null) {
			mOrignalThemeResourceId = resid;
		} else {
			super.setTheme(resid);
		}
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		mOrignalContext = newBase;
	}

	@Override
	public Context getApplicationContext() {
		if (getBaseContext() == null) {
			return mOrignalContext.getApplicationContext();
		} else {
			return super.getApplicationContext();
		}
	}

	@Override
	public Object getSystemService(String name) {
		if (getBaseContext() == null) {
			return mOrignalContext.getSystemService(name);
		} else {
			return super.getSystemService(name);
		}
	}

	@Override
	public Resources getResources() {
		if (Build.VERSION.SDK_INT < 14) {
			if (getBaseContext() == null) {
				return mOrignalContext.getResources();
			} else {
				return super.getResources();
			}
		} else {
			return super.getResources();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onRestart", new Class[] {}, new Object[] {});
	}

	@Override
	protected void onStart() {
		super.onStart();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onStart", new Class[] {}, new Object[] {});
	}

	@Override
	protected void onResume() {
		super.onResume();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onResume", new Class[] {}, new Object[] {});
	}
	
    @Override
    protected void onPostResume() {
    	super.onPostResume();
    	RefInvoker.invokeMethod(activity, Activity.class.getName(), "onPostResume", new Class[] {}, new Object[] {});
    }

	@Override
	protected void onPause() {
		super.onPause();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onPause", new Class[] {}, new Object[] {});
	}

	@Override
	protected void onStop() {
		super.onStop();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onStop", new Class[] {}, new Object[] {});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onDestroy", new Class[] {}, new Object[] {});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return (Boolean) RefInvoker.invokeMethod(activity, Activity.class.getName(), "onCreateOptionsMenu",
				new Class[] { Menu.class }, new Object[] { menu });
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (Boolean) RefInvoker.invokeMethod(activity, Activity.class.getName(), "onOptionsItemSelected",
				new Class[] { MenuItem.class }, new Object[] { item });
	}

	@Override
	protected void onNewIntent(Intent intent) {
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onNewIntent", new Class[] { Intent.class },
				new Object[] { intent });
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return (Boolean) RefInvoker.invokeMethod(activity, Activity.class.getName(), "onPrepareOptionsMenu",
				new Class[] { Menu.class }, new Object[] { menu });
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onRestoreInstanceState",
				new Class[] { Bundle.class }, new Object[] { savedInstanceState });
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onSaveInstanceState",
				new Class[] { Bundle.class }, new Object[] { outState });
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		RefInvoker.invokeMethod(activity, Activity.class.getName(), "onActivityResult", new Class[] { Integer.class,
				Integer.class, Intent.class, }, new Object[] { requestCode, resultCode, data });
	}
    
	/**
	 * 这里需要attach的值 根据不同的android版本和厂商版本，可能会有所不同 需要实际测试后才能保证兼容
	 * 
	 * 这里后续还需要考虑对不同android版本的差异性支持，attach的代码是基于Android L的实现
	 * 
	 * 重要：由于作者设备有限，以下内容仅在 小米2s 4.1.1 系统上测试过。
	 * 
	 */
	private void attach() {

		RefInvoker.invokeMethod(activity, ContextWrapper.class.getName(), "attachBaseContext",
				new Class[] { Context.class }, new Object[] { getBaseContext() });
		set("mWindow");
		set("mUiThread");
		set("mMainThread");
		set("mInstrumentation");
		set("mToken");
		set("mIdent");
		set("mApplication");
		set("mIntent");
		set("mComponent");
		set("mActivityInfo");
		set("mTitle");
		set("mParent");
		set("mEmbeddedID");
		set("mLastNonConfigurationInstances");
		// set("mVoiceInteractor");
		set("mFragments");
		set("mWindowManager");
		set("mCurrentConfig");
	}

	private void set(String name) {
		LogUtil.d("attach " + name);
		Object obj = RefInvoker.getFieldObject(this, Activity.class.getName(), name);
		if (obj != null) {
			RefInvoker.setFieldObject(activity, Activity.class.getName(), name, obj);
		}
	}

	private void loadPluginActivity() {
		try {
			String classId = getIntent().getStringExtra(PluginDispatcher.ACTIVITY_ID_IN_PLUGIN);
			LogUtil.d("classId ", classId);
			@SuppressWarnings("rawtypes")
			Class clazz = PluginLoader.loadPluginClassById(classId);
			activity = (Activity) clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
