/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limpoxe.fairy.core;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;

import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.LogUtil;

import java.util.ArrayList;

/**
 * 这里不必重写StartActivity的相关方法，是因为所有从Context.startActivity发起的调用，
 * 最后都会调用mMainThread.getInstrumentation().execStartActivity来执行
 * 而框架替换了Instrumentation，因此所有的StartActivity相关方法都会在PluginInstrumentation中做处理
 */
public class PluginBaseContextWrapper extends ContextWrapper {

	public PluginBaseContextWrapper(Context base) {
		super(base);
	}

	@Override
	public void sendBroadcast(Intent intent) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcast(item);
		}
	}

	@Override
	public void sendBroadcast(Intent intent, String receiverPermission) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcast(item, receiverPermission);
		}
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcast(item, receiverPermission);
		}
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcast(item, receiverPermission, resultReceiver,
					scheduler, initialCode, initialData, initialExtras);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcastAsUser(item, user);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcastAsUser(item, user, receiverPermission);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission,
			BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData,
			Bundle initialExtras) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcastAsUser(item, user, receiverPermission, resultReceiver, scheduler, initialCode,
					initialData, initialExtras);
		}
	}

	@Override
	public void sendStickyBroadcast(Intent intent) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyBroadcast(item);
		}
	}

	@Override
	public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler,
			int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyOrderedBroadcast(item, resultReceiver, scheduler, initialCode, initialData, initialExtras);
		}

	}

	@Override
	public void removeStickyBroadcast(Intent intent) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.removeStickyBroadcast(item);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyBroadcastAsUser(item, user);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.v(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyOrderedBroadcastAsUser(item, user, resultReceiver, scheduler, initialCode, initialData,
					initialExtras);
		}
	}

	@Override
	public ComponentName startService(Intent service) {
		LogUtil.v(service);
		PluginIntentResolver.resolveService(service);
		return super.startService(service);
	}

	@Override
	public boolean stopService(Intent name) {
		LogUtil.v(name);
		PluginIntentResolver.resolveService(name);
		return super.stopService(name);
	}

	@Override
	public boolean bindService(Intent service, ServiceConnection conn, int flags) {
		LogUtil.v(service);
		PluginIntentResolver.resolveService(service);
		return super.bindService(service, conn, flags);
	}

	@Override
	public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
		//这个方法有2个作用
		// 1、context返回插件宿主packageName时,安装插件中的contentprovider时会用到它，
		// 被android.app.ActiviThread这个类调用。
		// 2、可以方便的创建一个插件ApplicationContext副本。用于满足一些特定的业务需要
		if (PluginManagerHelper.getPluginDescriptorByPluginId(packageName) != null) {
            LogUtil.v(packageName, "创建插件Context");
            return PluginCreator.getNewPluginApplicationContext(packageName);
		} else {
            LogUtil.v(packageName, "创建正常Context");
        }
		return super.createPackageContext(packageName, flags);
	}
}
