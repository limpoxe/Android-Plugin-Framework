package com.limpoxe.fairy.manager.mapping;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginActivityInfo;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ResourceUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static com.limpoxe.fairy.manager.mapping.PluginStubBinding.buildDefaultAction;

public class StubActivityMappingProcessor implements StubMappingProcessor {

    /**
     * key:stub Activity Name
     * value:plugin Activity Name
     */
    private static HashMap<String, String> singleTaskActivityMapping = new HashMap<String, String>();
    private static HashMap<String, String> singleTopActivityMapping = new HashMap<String, String>();
    private static HashMap<String, String> singleInstanceActivityMapping = new HashMap<String, String>();
    private static String standardActivity = null;
    private static String standardLandspaceActivity = null;
    private static String standardActivityTranslucent = null;

    private static boolean isPoolInited = false;

    private static int sResId = -1;

    @Override
    public String bindStub(PluginDescriptor pluginDescriptor, String pluginActivityClassName) {
        if (StubExact.isExact(pluginActivityClassName, PluginDescriptor.ACTIVITY)) {
            return pluginActivityClassName;
        }
        initStubPool();

        PluginActivityInfo info = pluginDescriptor.getActivityInfos().get(pluginActivityClassName);

        HashMap<String, String> bindingMapping = null;
        int launchMode = Integer.parseInt(info.getLaunchMode());

        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {

            if (info.getTheme() != null) {
                LoadedPlugin loadedPlugin = PluginLauncher.instance().getRunningPlugin(pluginDescriptor.getPackageName());
                if (loadedPlugin != null) {
                    try {
                        if (sResId == -1) {
                            Class r = Class.forName("com.android.internal.R$attr");
                            Field f = r.getDeclaredField("windowIsTranslucent");
                            f.setAccessible(true);
                            sResId = (int)f.get(null);
                        }
                        int styleId = ResourceUtil.parseResId(info.getTheme());
                        if (styleId != 0) {
                            //maybe need cache
                            //根据目标Activity的主题id构造一个主题对象，
                            //并尝试从此主题中取出用于配置透明的属性：windowIsTranslucent
                            //如果取到了，说明目标Activity是使用的透明主题
                            //则返回透明主题的stubActivity
                            Resources.Theme theme = loadedPlugin.pluginResource.newTheme();
                            Resources.Theme baseTheme = ((ContextWrapper)loadedPlugin.pluginContext).getBaseContext().getTheme();
                            if (baseTheme != null) {
                                theme.setTo(baseTheme);
                            }
                            theme.applyStyle(styleId, true);
                            TypedArray a = theme.obtainStyledAttributes(null, new int[]{sResId}, 0, 0);
                            if (a.hasValue(0)) {
                                return standardActivityTranslucent;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.e("插件尚未运行，无法获取pluginResource对象");
                }
            }

            if (info.getScreenOrientation() != null && Integer.parseInt(info.getScreenOrientation()) == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                return standardLandspaceActivity;
            }

            return standardActivity;

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

            bindingMapping = singleTaskActivityMapping;

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

            bindingMapping = singleTopActivityMapping;

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

            bindingMapping = singleInstanceActivityMapping;

        }

        if (bindingMapping != null) {

            Iterator<Map.Entry<String, String>> itr = bindingMapping.entrySet().iterator();
            String idleStubActivityName = null;

            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                if (entry.getValue() == null) {
                    if (idleStubActivityName == null) {
                        idleStubActivityName = entry.getKey();
                        //这里找到空闲的stubactivity以后，还需继续遍历，用来检查是否pluginActivityClassName已经绑定过了
                    }
                } else if (pluginActivityClassName.equals(entry.getValue())) {
                    //已绑定过，直接返回
                    return entry.getKey();
                }
            }

            //没有绑定到StubActivity，而且还有空余的stubActivity，进行绑定
            if (idleStubActivityName != null) {
                bindingMapping.put(idleStubActivityName, pluginActivityClassName);
                return idleStubActivityName;
            }

        }

        return standardActivity;
    }

    @Override
    public void unBindStub(String stubActivityName, String pluginActivityName) {
        initStubPool();

        LogUtil.v("unBindLaunchModeStubActivity", stubActivityName, pluginActivityName);

        if (pluginActivityName.equals(singleTaskActivityMapping.get(stubActivityName))) {

            LogUtil.v("unBindLaunchModeStubActivity", stubActivityName, pluginActivityName);
            singleTaskActivityMapping.put(stubActivityName, null);

        } else if (pluginActivityName.equals(singleInstanceActivityMapping.get(stubActivityName))) {

            LogUtil.v("unBindLaunchModeStubActivity", stubActivityName, pluginActivityName);
            singleInstanceActivityMapping.put(stubActivityName, null);

        } else {
            //对于standard和singleTop的launchmode，不做处理。
        }
    }

    @Override
    public boolean isStub(String className) {
        initStubPool();

        return className.equals(standardActivity)
                || className.equals(standardLandspaceActivity)
                || className.equals(standardActivityTranslucent)
                || singleTaskActivityMapping.containsKey(className)
                || singleTopActivityMapping.containsKey(className)
                || singleInstanceActivityMapping.containsKey(className);
    }

    @Override
    public String getBindedPluginClassName(String stubClassName) {
        //not need
        return null;
    }

    @Override
    public int getType() {
        return StubMappingProcessor.TYPE_ACTIVITY;
    }

    private static void loadStubActivity() {
        Intent launchModeIntent = new Intent();
        launchModeIntent.setAction(buildDefaultAction());
        launchModeIntent.setPackage(FairyGlobal.getApplication().getPackageName());

        List<ResolveInfo> list = FairyGlobal.getApplication().getPackageManager().queryIntentActivities(launchModeIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (list != null && list.size() >0) {
            for (ResolveInfo resolveInfo:
                    list) {
                if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

                    singleTaskActivityMapping.put(resolveInfo.activityInfo.name, null);

                } else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

                    singleTopActivityMapping.put(resolveInfo.activityInfo.name, null);

                } else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

                    singleInstanceActivityMapping.put(resolveInfo.activityInfo.name, null);

                } else if (resolveInfo.activityInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {

                    if (resolveInfo.activityInfo.theme == android.R.style.Theme_Translucent) {
                        standardActivityTranslucent = resolveInfo.activityInfo.name;
                    } else if (resolveInfo.activityInfo.screenOrientation == SCREEN_ORIENTATION_LANDSCAPE) {
                        standardLandspaceActivity = resolveInfo.activityInfo.name;
                    } else {
                        standardActivity = resolveInfo.activityInfo.name;
                    }
                }

            }
        }

    }

    private static void initStubPool() {

        if (isPoolInited) {
            return;
        }

        loadStubActivity();

        isPoolInited = true;
    }
}
