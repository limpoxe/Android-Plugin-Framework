package com.limpoxe.fairy.core.viewfactory;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.limpoxe.fairy.util.LogUtil;

/**
 * Created by Administrator on 2015/12/13.
 *
 * Porting From SupportV7
 */
public class PluginViewFactory implements PluginFactoryInterface {

	final Activity mContext;
	final Window mWindow;
	final Window.Callback mOriginalWindowCallback;
	final LayoutInflater.Factory mViewfactory;

	PluginViewInflater mPluginViewInflater;

	public PluginViewFactory(Activity context, Window window, LayoutInflater.Factory viewfactory) {
		mContext = context;
		mWindow = window;
		mOriginalWindowCallback = window.getCallback();
		mViewfactory = viewfactory;
	}

	public void installViewFactory() {
		LogUtil.d("安装PluginViewFactory");
		LayoutInflater layoutInflater = mContext.getLayoutInflater();
		if (layoutInflater.getFactory() == null) {
			PluginFactoryCompat.setFactory(layoutInflater, this);
		} else {
			LogUtil.d("The Activity's LayoutInflater already has a Factory installed"
					+ " so we can not install plugin's");
		}
		LogUtil.d("安装PluginViewFactory完成");
	}

	@Override
	public final View onCreateView(View parent, String name,
								   Context context, AttributeSet attrs) {
		// First let the Activity's Factory try and inflate the view
		final View view = callActivityOnCreateView(parent, name, context, attrs);
		if (view != null) {
			return view;
		}

		// If the Factory didn't handle it, let our createView() method try
		return createView(parent, name, context, attrs);
	}

	private View callActivityOnCreateView(View parent, String name, Context context, AttributeSet attrs) {
		View view = null;
		if (mOriginalWindowCallback instanceof LayoutInflater.Factory) {
			view = ((LayoutInflater.Factory) mOriginalWindowCallback)
					.onCreateView(name, context, attrs);
		}

		if (view != null) {
			return view;
		}

		if(Build.VERSION.SDK_INT >= 11) {
			if (mOriginalWindowCallback instanceof LayoutInflater.Factory2) {
				return ((LayoutInflater.Factory2) mOriginalWindowCallback)
						.onCreateView(parent, name, context, attrs);
			}
		}

		return null;
	}

	private View createView(View parent, final String name, Context context,
						   AttributeSet attrs) {
		final boolean isPre21 = Build.VERSION.SDK_INT < 21;

		if (mPluginViewInflater == null) {
			mPluginViewInflater = new PluginViewInflater(mContext, mViewfactory);
		}

		// We only want the View to inherit it's context from the parent if it is from the
		// apps content, and not part of our sub-decor
		final boolean inheritContext = isPre21 && parent != null
				&& parent.getId() != android.R.id.content;

		return mPluginViewInflater.createView(parent, name, context, attrs,
				inheritContext, isPre21);
	}
}
