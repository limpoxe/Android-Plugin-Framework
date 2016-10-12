package com.limpoxe.fairy.core.loading;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.PluginContextTheme;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.core.PluginLoader;
import com.limpoxe.fairy.util.LogUtil;

/**
 * 这个页面要求尽可能的简单
 * Created by cailiming on 16/10/12.
 */

public class WaitForLoadingPluginActivity extends Activity {
    private PluginDescriptor pluginDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 是否需要全屏取决于上个页面是否为全屏,
        // 目的是和上个页面保持一致, 否则被透视的页面会发生移动
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int resId = PluginLoader.getLoadingResId();
        LogUtil.i("WaitForLoadingPluginActivity ContentView Id = " + resId);

        if (resId != 0) {
            setContentView(resId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i("WaitForLoadingPluginActivity Shown");
        if (pluginDescriptor != null && !PluginLauncher.instance().isRunning(pluginDescriptor.getPackageName())) {
            //连续调用此方法不会导致多次初始化
            PluginLauncher.instance().startPluginAsync(pluginDescriptor, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    LoadedPlugin loadedPlugin = (LoadedPlugin)msg.obj;
                    if (loadedPlugin.pluginApplication == null) {
                        PluginLauncher.instance().initApplication(loadedPlugin.pluginContext,
                                loadedPlugin.pluginClassLoader,
                                loadedPlugin.pluginContext.getResources(),
                                ((PluginContextTheme)loadedPlugin.pluginContext).getPluginDescriptor(),
                                loadedPlugin);
                    }

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(getIntent());
                            finish();
                        }
                    }, 300);
                }
            });
        } else {
            LogUtil.d("WTF!");
            //finish();
        }
    }

    public void setTargetPlugin(PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
    }
}
