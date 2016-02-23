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

package com.plugin.core;

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

import com.plugin.util.LogUtil;

import java.util.ArrayList;

public class PluginBaseContextWrapper extends ContextWrapper {

	public PluginBaseContextWrapper(Context base) {
		super(base);
	}

	/**
	 * startActivity有很多重载的方法，如有必要，可以相应的重写
	 */
	@Override
	public void startActivity(Intent intent) {
		LogUtil.d(intent);
		PluginIntentResolver.resolveActivity(intent);
		super.startActivity(intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void startActivity(Intent intent, Bundle options) {
		LogUtil.d(intent);
		PluginIntentResolver.resolveActivity(intent);
		super.startActivity(intent, options);
	}

	@Override
	public void sendBroadcast(Intent intent) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcast(item);
		}
	}

	@Override
	public void sendBroadcast(Intent intent, String receiverPermission) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcast(item, receiverPermission);
		}
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcast(item, receiverPermission);
		}
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcast(item, receiverPermission, resultReceiver,
					scheduler, initialCode, initialData, initialExtras);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendBroadcastAsUser(item, user);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
		LogUtil.d(intent);
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
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendOrderedBroadcastAsUser(item, user, receiverPermission, resultReceiver, scheduler, initialCode,
					initialData, initialExtras);
		}
	}

	@Override
	public void sendStickyBroadcast(Intent intent) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyBroadcast(item);
		}
	}

	@Override
	public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler,
			int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyOrderedBroadcast(item, resultReceiver, scheduler, initialCode, initialData, initialExtras);
		}

	}

	@Override
	public void removeStickyBroadcast(Intent intent) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.removeStickyBroadcast(item);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyBroadcastAsUser(item, user);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
		LogUtil.d(intent);
		ArrayList<Intent> list = PluginIntentResolver.resolveReceiver(intent);
		for (Intent item:list) {
			super.sendStickyOrderedBroadcastAsUser(item, user, resultReceiver, scheduler, initialCode, initialData,
					initialExtras);
		}
	}

	@Override
	public ComponentName startService(Intent service) {
		LogUtil.d(service);
		PluginIntentResolver.resolveService(service);
		return super.startService(service);
	}

	@Override
	public boolean stopService(Intent name) {
		LogUtil.d(name);
		PluginIntentResolver.resolveService(name);
		return super.stopService(name);
	}

	@Override
	public boolean bindService(Intent service, ServiceConnection conn, int flags) {
		LogUtil.d(service);
		PluginIntentResolver.resolveService(service);
		return super.bindService(service, conn, flags);
	}

	@Override
	public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
		if (PluginLoader.getPluginDescriptorByPluginId(packageName) != null) {
			PluginLoader.ensurePluginInited(packageName);
			return PluginLoader.getNewPluginApplicationContext(packageName);
		}
		return super.createPackageContext(packageName, flags);
	}
}
