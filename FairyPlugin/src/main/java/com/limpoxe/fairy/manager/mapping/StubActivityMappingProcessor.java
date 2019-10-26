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
import java.util.ArrayList;
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
    private static HashMap<String, List<String>> singleTaskActivityMapping = new HashMap<String, List<String>>();
    private static HashMap<String, List<String>> singleTopActivityMapping = new HashMap<String, List<String>>();
    private static HashMap<String, List<String>> singleInstanceActivityMapping = new HashMap<String, List<String>>();
    private static HashMap<String, List<String>> standardActivityMapping = new HashMap<String, List<String>>();
    private static HashMap<String, List<String>> standardActivityTranslucentMapping = new HashMap<String, List<String>>();

    private static String standardLandspaceActivity = null;

    private static boolean isPoolInited = false;

    private static int sResId = -1;

    @Override
    public String bindStub(PluginDescriptor pluginDescriptor, String pluginActivityClassName) {
        if (StubExact.isExact(pluginActivityClassName, PluginDescriptor.ACTIVITY)) {
            return pluginActivityClassName;
        }
        initStubPool();

        PluginActivityInfo info = pluginDescriptor.getActivityInfos().get(pluginActivityClassName);

        HashMap<String, List<String>> bindingMapping = null;
        int launchMode = (int)Long.parseLong(info.getLaunchMode());

        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {

            //如果是横屏
            if (info.getScreenOrientation() != null && Integer.parseInt(info.getScreenOrientation()) == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                return standardLandspaceActivity;
            }

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
                                bindingMapping = standardActivityTranslucentMapping;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        LogUtil.printException("StubActivityMappingProcessor.bindStub", e);
                    } catch (IllegalAccessException e) {
                        LogUtil.printException("StubActivityMappingProcessor.bindStub", e);
                    } catch (NoSuchFieldException e) {
                        LogUtil.printException("StubActivityMappingProcessor.bindStub", e);
                    }
                } else {
                    LogUtil.e("插件尚未运行，无法获取pluginResource对象", pluginDescriptor.getPackageName());
                }
            }

            if (bindingMapping == null) {
                bindingMapping = standardActivityMapping;
            }

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

            bindingMapping = singleTaskActivityMapping;

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

            bindingMapping = singleTopActivityMapping;

        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {

            bindingMapping = singleInstanceActivityMapping;

        }

        if (bindingMapping != null) {

            Iterator<Map.Entry<String, List<String>>> itr = bindingMapping.entrySet().iterator();
            String idleStubActivityName = null;

            while (itr.hasNext()) {
                Map.Entry<String, List<String>> entry = itr.next();
                List<String> list = entry.getValue();
                if (list == null || list.size() == 0) {
                    if (idleStubActivityName == null) {
                        idleStubActivityName = entry.getKey();
                        //这里找到空闲的stubactivity以后，还需继续遍历，用来检查是否pluginActivityClassName已经绑定过了
                    }
                } else {
                    if (list.get(0).equals(pluginActivityClassName)) {
                        //已绑定过，直接返回
                        return entry.getKey();
                    }
                }
            }

            //没有绑定到StubActivity，而且还有空余的stubActivity，进行绑定
            if (idleStubActivityName != null) {
                List<String> list = bindingMapping.get(idleStubActivityName);
                if (list == null) {
                    list = new ArrayList<String>();
                    bindingMapping.put(idleStubActivityName, list);
                }
                list.add(pluginActivityClassName);
                return idleStubActivityName;
            } else {
                //stub 不够用了
                LogUtil.e("stub不够用了，需要继续扩展stub占坑");
            }

        }

        //todo 或许需要一个default更保险一点？
        return "";
    }

    @Override
    public void unBindStub(String stubActivityName, String pluginActivityName) {
        initStubPool();

        LogUtil.v("unBindLaunchModeStubActivity", stubActivityName, pluginActivityName);

        if (reduce(singleTaskActivityMapping.get(stubActivityName), pluginActivityName)) {
            return;
        }

        if (reduce(singleInstanceActivityMapping.get(stubActivityName), pluginActivityName)) {
            return;
        }

        if (reduce(standardActivityMapping.get(stubActivityName), pluginActivityName)) {
            return;
        }

        if (reduce(standardActivityTranslucentMapping.get(stubActivityName), pluginActivityName)) {
            return;
        }

        if (reduce(singleTopActivityMapping.get(stubActivityName), pluginActivityName)) {
            return;
        }
    }

    private boolean reduce(List<String> pluginActivityList, String pluginActivityName) {
        if (pluginActivityList != null && pluginActivityList.size() > 0 && pluginActivityList.get(0).equals(pluginActivityName)) {
            LogUtil.v("unBindLaunchModeStubActivity", pluginActivityName);
            pluginActivityList.remove(pluginActivityName);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStub(String className) {
        initStubPool();

        return standardActivityMapping.containsKey(className)
                || className.equals(standardLandspaceActivity)
                || standardActivityTranslucentMapping.containsKey(className)
                || singleTaskActivityMapping.containsKey(className)
                || singleTopActivityMapping.containsKey(className)
                || singleInstanceActivityMapping.containsKey(className);
    }

    @Override
    public String getBindedPluginClassName(String stubClassName) {

        //桥接的，不需要反查
        if(StubExact.isExact(stubClassName, PluginDescriptor.UNKOWN)) {
            return stubClassName;
        }

        if (stubClassName.equals(standardLandspaceActivity)) {
            //1对多的，没法反查
            return stubClassName;
        }

        String target = searchMapping(standardActivityMapping, stubClassName);
        if (target == null) {
            target = searchMapping(standardActivityTranslucentMapping, stubClassName);
        }
        if (target == null) {
            target = searchMapping(singleTaskActivityMapping, stubClassName);
        }
        if (target == null) {
            target = searchMapping(singleTopActivityMapping, stubClassName);
        }
        if (target == null) {
            target = searchMapping(singleInstanceActivityMapping, stubClassName);
        }
        if (target == null) {
            target = stubClassName;
        }
        return target;
    }

    private String searchMapping(HashMap<String, List<String>> bindingMapping, String stubClassName) {
        if (bindingMapping != null) {
            Iterator<Map.Entry<String, List<String>>> itr = bindingMapping.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, List<String>> entry = itr.next();
                String stubName = entry.getKey();
                if (stubName.equals(stubClassName)) {
                    List<String> list = entry.getValue();
                    if (list != null && list.size() > 0) {
                        return list.get(0);
                    } else {
                        LogUtil.e("searchMapping fail, stubClassName : " + stubClassName + " not bind anything");
                        return null;
                    }
                }
            }
        }
        LogUtil.e("searchMapping fail, bindingMapping is NULL");
        return null;
    }

    @Override
    public int getType() {
        return StubMappingProcessor.TYPE_ACTIVITY;
    }

    private static void loadStubActivity() {
        Intent launchModeIntent = new Intent();
        launchModeIntent.setAction(buildDefaultAction());
        launchModeIntent.setPackage(FairyGlobal.getHostApplication().getPackageName());

        List<ResolveInfo> list = FairyGlobal.getHostApplication().getPackageManager().queryIntentActivities(launchModeIntent, PackageManager.MATCH_DEFAULT_ONLY);

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

                        standardActivityTranslucentMapping.put(resolveInfo.activityInfo.name, null);

                    } else if (resolveInfo.activityInfo.screenOrientation == SCREEN_ORIENTATION_LANDSCAPE) {

                        standardLandspaceActivity = resolveInfo.activityInfo.name;

                    } else {

                        standardActivityMapping.put(resolveInfo.activityInfo.name, null);

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
