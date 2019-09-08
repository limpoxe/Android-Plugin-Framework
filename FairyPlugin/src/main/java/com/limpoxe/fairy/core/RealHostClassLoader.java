package com.limpoxe.fairy.core;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import com.limpoxe.fairy.core.bridge.PluginShadowService;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import dalvik.system.DexClassLoader;

/**
 * 为了支持Receiver和Service，增加此类。
 * 
 * @author Administrator
 * 
 */
public class RealHostClassLoader extends DexClassLoader {

    public RealHostClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    @Override
    public String findLibrary(String name) {
        LogUtil.v("findLibrary", name);
        return super.findLibrary(name);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

        //Just for Receiver and Service

        if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_SERVICE)) {

            LogUtil.v("className ", className);

            // 这里返回PluginShadowService是因为service的构造函数以及onCreate函数
            // 2个函数在ActivityThread的同一个函数中被调用,框架没机会在构造器执行之后,oncreate执行之前,
            // 插入一段代码, 注入context.
            // 因此这里返回一个fake的service, 在fake service的oncreate方法里面手动调用构造器和oncreate
            // 这里返回了这个Service以后, 由于在框架中hook了ActivityManager的serviceDoneExecuting方法,
            // 在serviceDoneExecuting这个方法里面, 会将这个service再还原成插件的servcie对象
            if (!className.equals(PluginIntentResolver.CLASS_PREFIX_SERVICE_NOT_FOUND)) {
                return PluginShadowService.class;
            }

            LogUtil.e("到了这里说明出bug了,这里做个容错处理, 避免出现classnotfound", className);
            return RealHostClassLoader.TolerantService.class;

        } else if (className.startsWith(PluginIntentResolver.CLASS_PREFIX_RECEIVER)) {

            LogUtil.v("className ", className);

            if (!className.equals(PluginIntentResolver.CLASS_PREFIX_RECEIVER_NOT_FOUND)) {
                String realName = className.replace(PluginIntentResolver.CLASS_PREFIX_RECEIVER, "");
                Class clazz = PluginLoader.loadPluginClassByName(realName);
                if (clazz != null) {
                    return clazz;
                }
            }

            LogUtil.e("到了这里说明出bug了,这里做个容错处理, 避免出现classnotfound", className);
            return RealHostClassLoader.TolerantBroadcastReceiver.class;
        }

        //如果这里出现classnotfound，但是className确实是一个插件的receiver或者service，
        //那么很可能是PluginAppTrace没有替换成功，或者替换成功了但是又被其他东西覆盖替换掉了。
        return super.loadClass(className, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        //宿主Activity内嵌几个来自插件的Fragment时
        //如果这个宿主Activity在后台被系统回收了，用户重新回到这个Activity时系统自动恢复被回收的Activity
        //同时这个Activity内如果有Fragment也会被自动恢复，如果被恢复的Fragment是来自插件，则会发送ClassNotFound
        //因为恢复Fragment时使用的ClassLoader就是当前宿主Activity的getClassLoader
        //重写这个方法，就是为了处理这个问题，使得自动恢复Fragment不会产生ClassNotFound
        //这里只关心被列入插件Manifest中的组件的类
        if(ProcessUtil.isPluginProcess()) {
            Class clazz = PluginLoader.loadPluginClassByName(name);
            if (clazz != null) {
                return clazz;
            }
        }
        return super.findClass(name);
    }

    public static class TolerantBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.w("容错TolerantBroadcastReceiver被触发");
        }
    }

    public static class TolerantService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LogUtil.w("容错TolerantService被触发");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

    public static class TolerantActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtil.w("容错TolerantActivity被触发");
        }

        @Override
        protected void onResume() {
            super.onResume();
            finish();

            Toast.makeText(this, "正在退出...", Toast.LENGTH_LONG).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtil.w("killProcess，exit");
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                }
            }, 1000);
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            LogUtil.w("killProcess，exit");
            Process.killProcess(Process.myPid());
            System.exit(10);
        }
    }

}
