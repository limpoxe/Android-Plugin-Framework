package com.example.plugintest.service;

import com.example.plugintest.R;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author cailiming
 * 
 */
public class PluginTestService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("PluginTestService", " onStartCommand " 
					+ (intent == null?" null" : intent.toUri(0)));
		
		Toast.makeText(this, " PluginTestService " 
					+ (intent == null?" null" : intent.toUri(0)), Toast.LENGTH_LONG).show();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
