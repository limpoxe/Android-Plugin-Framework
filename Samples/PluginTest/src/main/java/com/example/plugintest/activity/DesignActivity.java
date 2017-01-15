package com.example.plugintest.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.plugintest.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 包含Design控件的主题和style在5.x及以上的系统可以正常使用，
 * 但是对4.x以下部分属性需要显式配置，如背景色、文字颜色等等,
 * 可能是布局文件中的android:theme配置造成。
 * 宿主中的android:theme配置在编译时固化为宿主中的定义，未使用插件中的定义
 */
public class DesignActivity extends AppCompatActivity {

	private DrawerLayout mDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.design_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final ActionBar ab = getSupportActionBar();
		ab.setHomeAsUpIndicator(R.drawable.ic_menu);
		ab.setDisplayHomeAsUpEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		if (navigationView != null) {
			setupDrawerContent(navigationView);
		}

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		if (viewPager != null) {
			setupViewPager(viewPager);
		}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar snackbar = Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
						.setAction("Action", null);

				View snapBarContentView = snackbar.getView();
				snapBarContentView.setBackgroundColor(getResources().getColor(R.color.text_color));
				TextView textView = (TextView) snapBarContentView.findViewById(com.example.pluginmain.R.id.snackbar_text);
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				snackbar.show();
			}
		});

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sample_actions, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (AppCompatDelegate.getDefaultNightMode()) {
			case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
				menu.findItem(R.id.menu_night_mode_system).setChecked(true);
				break;
			case AppCompatDelegate.MODE_NIGHT_AUTO:
				menu.findItem(R.id.menu_night_mode_auto).setChecked(true);
				break;
			case AppCompatDelegate.MODE_NIGHT_YES:
				menu.findItem(R.id.menu_night_mode_night).setChecked(true);
				break;
			case AppCompatDelegate.MODE_NIGHT_NO:
				menu.findItem(R.id.menu_night_mode_day).setChecked(true);
				break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.menu_night_mode_system:
				setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				break;
			case R.id.menu_night_mode_day:
				setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;
			case R.id.menu_night_mode_night:
				setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;
			case R.id.menu_night_mode_auto:
				setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
		AppCompatDelegate.setDefaultNightMode(nightMode);

		if (Build.VERSION.SDK_INT >= 11) {
			recreate();
		}
	}

	private void setupViewPager(ViewPager viewPager) {
		Adapter adapter = new Adapter(getSupportFragmentManager());
		adapter.addFragment(new ListFragment(), "Category 1");
		adapter.addFragment(new ListFragment(), "Category 2");
		adapter.addFragment(new ListFragment(), "Category 3");
		viewPager.setAdapter(adapter);
	}

	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						menuItem.setChecked(true);
						mDrawerLayout.closeDrawers();
						return true;
					}
				});
	}

	static class Adapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();

		public Adapter(FragmentManager fm) {
			super(fm);
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}
	}

	public static class ListFragment extends Fragment {

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			TextView view = new TextView(container.getContext());
			view.setText("ABCDEFG");
			view.setGravity(Gravity.CENTER);
			view.setTextColor(getResources().getColor(R.color.text_color));
			view.setBackgroundColor(getResources().getColor(R.color.light_btn_disable_color));
			return view;
		}

	}
}