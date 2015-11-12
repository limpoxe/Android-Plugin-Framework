package com.example.plugintest.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugintest.R;
import com.example.plugintest.service.IServiceProcessBindOne;
import com.example.plugintest.service.PluginServiceBindOne;
import com.example.plugintest.service.PluginServiceBindOne.MyBinderOne;
import com.example.plugintest.service.PluginServiceBindTwo;
import com.example.plugintest.service.PluginServiceBindTwo.MyBinderTwo;
import com.example.plugintest.service.PluginServiceOne;
import com.example.plugintest.service.PluginServiceProcessBindOne;
import com.example.plugintest.service.PluginServiceThree;
import com.example.plugintest.service.PluginServiceTwo;

public class PluginServiceActivity extends Activity implements View.OnClickListener{
    private static final String TAG = PluginServiceActivity.class.getSimpleName();

    Button buttonServiceOne;
    Button buttonServiceTwo;
    Button buttonServiceThree;
    Button buttonServiceBindOne;
    Button buttonServiceBindTwo;
    Button buttonServiceBindMethodOne;
    Button buttonServiceBindMethodTwo;
    Button buttonServiceProcessBindOne;
    Button buttonServiceProcessBindShow;
    Button buttonServiceProcessBindCalculate;

    private PluginServiceBindOne mServiceBindOne;
    private PluginServiceBindTwo mServiceBindTwo;
    private IServiceProcessBindOne mIServiceProcessBindOne;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        buttonServiceOne = (Button)findViewById(R.id.button_service_one);
        buttonServiceTwo = (Button)findViewById(R.id.button_service_two);
        buttonServiceThree = (Button)findViewById(R.id.button_service_three);
        buttonServiceBindOne = (Button)findViewById(R.id.button_service_bind_one);
        buttonServiceBindTwo = (Button)findViewById(R.id.button_service_bind_two);

        buttonServiceProcessBindOne = (Button)findViewById(R.id.button_service_process_bind_one);
        buttonServiceProcessBindShow = (Button)findViewById(R.id.button_service_process_bind_show);
        buttonServiceProcessBindCalculate = (Button)findViewById(R.id.button_service_process_bind_calculate);

        buttonServiceBindMethodOne = (Button)findViewById(R.id.button_service_bind_method_one);
        buttonServiceBindMethodTwo = (Button)findViewById(R.id.button_service_bind_method_two);
        buttonServiceOne.setOnClickListener(this);
        buttonServiceTwo.setOnClickListener(this);
        buttonServiceThree.setOnClickListener(this);
        buttonServiceBindOne.setOnClickListener(this);
        buttonServiceBindTwo.setOnClickListener(this);
        buttonServiceBindMethodOne.setOnClickListener(this);
        buttonServiceBindMethodTwo.setOnClickListener(this);
        buttonServiceProcessBindOne.setOnClickListener(this);
        buttonServiceProcessBindShow.setOnClickListener(this);
        buttonServiceProcessBindCalculate.setOnClickListener(this);
    }

    /**
     * 启动Service
     *
     * @param cls
     * @param params
     */
    private void startService(Class cls, String params) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("Params", params);
        startService(intent);
    }


    private ServiceConnection connOne = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            buttonServiceBindMethodOne.setVisibility(View.GONE);
            Toast.makeText(PluginServiceActivity.this, "BindService 失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinderOne binder = (MyBinderOne) service;
            mServiceBindOne = binder.getService();
            buttonServiceBindMethodOne.setVisibility(View.VISIBLE);
            Toast.makeText(PluginServiceActivity.this, "Service绑定成功:" + mServiceBindOne,Toast.LENGTH_SHORT).show();
        }
    };

    private ServiceConnection connTwo = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            buttonServiceBindMethodTwo.setVisibility(View.GONE);
            Toast.makeText(PluginServiceActivity.this, "BindService 失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinderTwo binder = (MyBinderTwo) service;
            mServiceBindTwo = binder.getService();
            buttonServiceBindMethodTwo.setVisibility(View.VISIBLE);
            Toast.makeText(PluginServiceActivity.this, "Service绑定成功:" + mServiceBindTwo,Toast.LENGTH_SHORT).show();
        }
    };

    private ServiceConnection connProcessOne = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            buttonServiceProcessBindShow.setVisibility(View.GONE);
            buttonServiceProcessBindCalculate.setVisibility(View.GONE);
            Toast.makeText(PluginServiceActivity.this, "BindService 失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIServiceProcessBindOne = IServiceProcessBindOne.Stub.asInterface(service);
            buttonServiceProcessBindShow.setVisibility(View.VISIBLE);
            buttonServiceProcessBindCalculate.setVisibility(View.VISIBLE);
            Toast.makeText(PluginServiceActivity.this, "Service绑定成功:" + mIServiceProcessBindOne, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mServiceBindOne != null) {
            unbindService(connOne);
            Intent intent = new Intent(this, PluginServiceBindOne.class);
            stopService(intent);
        }

        if (mServiceBindTwo != null) {
            unbindService(connTwo);
            Intent intent = new Intent(this, PluginServiceBindTwo.class);
            stopService(intent);
        }

        if (mIServiceProcessBindOne != null) {
            unbindService(connProcessOne);
            Intent intent = new Intent(this, PluginServiceProcessBindOne.class);
            stopService(intent);
        }

    }

    @Override
    public void onClick(View v) {
        int pid = android.os.Process.myPid();
        int id = v.getId();
        switch (id) {
            case R.id.button_service_one://启动第一个Service
                startService(PluginServiceOne.class, "第一个 pid:" + pid);
                break;
            case R.id.button_service_two://启动第二个Service
                startService(PluginServiceTwo.class, "第二个pid:" + pid);
                break;
            case R.id.button_service_three://启动第二个Service
                startService(PluginServiceThree.class, "第三个pid:" + pid);
                break;
            case R.id.button_service_bind_one://绑定ServiceBindOne
                Intent bindOneIntent = new Intent(this, PluginServiceBindOne.class);
                bindService(bindOneIntent, connOne, Context.BIND_AUTO_CREATE);
                break;
            case R.id.button_service_bind_method_one:
                if (mServiceBindOne != null) {
                    mServiceBindOne.showToast();
                }
                break;
            case R.id.button_service_bind_two://绑定ServiceBindTwo
                Intent bindTwoIntent = new Intent(this, PluginServiceBindTwo.class);
                bindService(bindTwoIntent, connTwo, Context.BIND_AUTO_CREATE);
                break;
            case R.id.button_service_bind_method_two:
                if (mServiceBindTwo != null) {
                    mServiceBindTwo.showToast();
                }
                break;
            case R.id.button_service_process_bind_one://绑定跨进程ServiceProcessBindOne
                Intent bindProcessIntent = new Intent(this, PluginServiceProcessBindOne.class);
                bindService(bindProcessIntent, connProcessOne, Context.BIND_AUTO_CREATE);
                break;
            case R.id.button_service_process_bind_show:
                if (mIServiceProcessBindOne != null) {
                    try {
                        mIServiceProcessBindOne.showToast();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.button_service_process_bind_calculate:
                if (mIServiceProcessBindOne != null) {
                    try {
                        int result = mIServiceProcessBindOne.calculate(3, 6);
                        Toast.makeText(this, "计算3+6，结果：" + result,Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
