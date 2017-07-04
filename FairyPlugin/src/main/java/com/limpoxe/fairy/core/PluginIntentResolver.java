package com.limpoxe.fairy.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.limpoxe.fairy.content.PluginActivityInfo;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.content.PluginReceiverIntent;
import com.limpoxe.fairy.core.android.HackCreateServiceData;
import com.limpoxe.fairy.core.android.HackReceiverData;
import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.manager.PluginProviderClient;
import com.limpoxe.fairy.util.LogUtil;
import com.limpoxe.fairy.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Iterator;

public class PluginIntentResolver {

	public static final String CLASS_SEPARATOR = "@";//字符串越短,判断时效率越高
	public static final String CLASS_PREFIX_RECEIVER = "#";//字符串越短,判断时效率越高
	public static final String CLASS_PREFIX_SERVICE = "%";//字符串越短,判断时效率越高

    public static final String CLASS_PREFIX_RECEIVER_NOT_FOUND = CLASS_PREFIX_RECEIVER + "NOT_FOUND";
    public static final String CLASS_PREFIX_SERVICE_NOT_FOUND = CLASS_PREFIX_SERVICE + "NOT_FOUND";

    public static void resolveService(Intent intent) {
		ArrayList<String> classNameList = matchPlugin(intent, PluginDescriptor.SERVICE);
		if (classNameList != null && classNameList.size() > 0) {
            //TODO 只取第一个，忽略了多Service匹配到同一个Intent的情况
            String stubServiceName = PluginProviderClient.bindStubService(classNameList.get(0));
			if (stubServiceName != null) {
				intent.setComponent(new ComponentName(FairyGlobal.getApplication().getPackageName(), stubServiceName));
			}
		} else {
			if (intent.getComponent() != null && null != PluginManagerHelper.getPluginDescriptorByPluginId(intent.getComponent().getPackageName())) {
				intent.setComponent(new ComponentName(FairyGlobal.getApplication().getPackageName(), intent.getComponent().getClassName()));
			}
		}
	}

	public static ArrayList<Intent> resolveReceiver(final Intent intent) {
		// 如果在插件中发现了匹配intent的receiver项目，替换掉ClassLoader
		// 不需要在这里记录目标className，className将在Intent中传递
		ArrayList<Intent> result = new ArrayList<Intent>();
		ArrayList<String> classNameList = matchPlugin(intent, PluginDescriptor.BROADCAST);
		if (classNameList != null && classNameList.size() > 0) {
			for(String className: classNameList) {
				Intent newIntent = new Intent(intent);
				newIntent.setComponent(new ComponentName(FairyGlobal.getApplication().getPackageName(),
                        //因为此时已经在插件中匹配到intent，
                        //此时不用关心原intent是不是精确匹配的intent，到了这一步时，是将目标替换为stub还是exact已经无所谓了，
                        // resolveReceiverForClassLoader都可以拿到真实classname
                        //只需给出默认的stubReceiver即可，因此这里的参数使用null
                        PluginProviderClient.bindStubReceiver(null)));
				//hackReceiverForClassLoader检测到这个标记后会进行替换
				newIntent.setAction(className + CLASS_SEPARATOR + (intent.getAction() == null ? "" : intent.getAction()));
				result.add(newIntent);
			}
		} else {
			if (intent.getComponent() != null && null != PluginManagerHelper.getPluginDescriptorByPluginId(intent.getComponent().getPackageName())) {
                //如果intent是指向插件的，但是matchPlugin又没有找到目标，这时强行修正intent指向宿主
                intent.setComponent(new ComponentName(FairyGlobal.getApplication().getPackageName(), intent.getComponent().getClassName()));
			}
		}

		//fix 插件中对同一个广播同时注册了动态和静态广播的情况
		result.add(intent);

		return result;
	}

	/* package */static Context resolveReceiverForClassLoader(final Object msgObj) {

        if (ProcessUtil.isPluginProcess()) {

            PluginInjector.hackHostClassLoaderIfNeeded();

            HackReceiverData hackReceiverData = new HackReceiverData(msgObj);
            Intent intent = hackReceiverData.getIntent();
            //className要么是真组件，要么是stub，要么是exact的组件
            String className = intent.getComponent().getClassName();
            //当是stub或者exact时，需要处理className，供classloader使用
            if (PluginProviderClient.isStub(className)) {
                String realReceiverClassName = null;
                String[] targetClassName = null;
                if (PluginProviderClient.isExact(className, PluginDescriptor.BROADCAST)) {
                    realReceiverClassName = className;
                } else {
                    String action = intent.getAction();
                    if (action != null) {
                        targetClassName = action.split(CLASS_SEPARATOR);
                        realReceiverClassName = targetClassName[0];
                    }
                }
                if (realReceiverClassName == null) {
                    return null;
                }

                @SuppressWarnings("rawtypes")
                Class clazz = PluginLoader.loadPluginClassByName(realReceiverClassName);

                if (clazz != null) {
                    intent.setExtrasClassLoader(clazz.getClassLoader());
                    if (targetClassName != null) {
                        //由于之前intent被修改过 这里再吧Intent还原到原始的intent
                        if (targetClassName.length > 1) {
                            intent.setAction(targetClassName[1]);
                        } else {//length等于1的情况是因为原始的intent可能不是通过Action过来的，而是直接通过Component过来的
                            intent.setAction(null);
                        }
                    } else {
                        //isExact 无需对intent进行恢复
                    }

                    // HostClassLoader检测到这个特殊标记后会进行替换，得到真实的className
                    intent.setComponent(new ComponentName(intent.getComponent().getPackageName(), CLASS_PREFIX_RECEIVER + realReceiverClassName));

                    if (Build.VERSION.SDK_INT >= 21) {
                        if (intent.getExtras() != null) {
                            hackReceiverData.setIntent(new PluginReceiverIntent(intent));
                        }
                    }
                    return PluginLoader.getDefaultPluginContext(clazz);
                } else {
                    //在未安装插件的情况下收到了由宿主桥接到插件的广播，例如开关机广播，会到这里来
                    if(targetClassName == null) {
                        LogUtil.w("返回容错标记， 交给HostClassLoader处理");
                        intent.setComponent(new ComponentName(intent.getComponent().getPackageName(), CLASS_PREFIX_RECEIVER_NOT_FOUND));
                    }
                }
            }
        }
		return null;
	}

	/* package */static String resolveServiceForClassLoader(Object msgObj) {

		HackCreateServiceData hackCreateServiceData = new HackCreateServiceData(msgObj);
		ServiceInfo info = hackCreateServiceData.getInfo();

        if (info == null) {
            LogUtil.e("反射失败？");
            return null;
        }

		if (ProcessUtil.isPluginProcess()) {

			PluginInjector.hackHostClassLoaderIfNeeded();

			//通过映射查找
			String targetClassName = PluginProviderClient.getBindedPluginServiceName(info.name);
			//TODO 或许可以通过这个方式来处理service
			//info.applicationInfo = XXX

			LogUtil.v("hackServiceName", info.name, info.packageName, info.processName, "targetClassName", targetClassName, info.applicationInfo.packageName);

			if (targetClassName != null) {
				info.name =  CLASS_PREFIX_SERVICE + targetClassName;
			} else if (PluginProviderClient.isStub(info.name)) {
				String dumpString = PluginProviderClient.dumpServiceInfo();
				LogUtil.w("没有找到映射关系, 可能映射表出了异常", info.name, dumpString);
                LogUtil.w("返回容错标记， 交给HostClassLoader处理");
                info.name = CLASS_PREFIX_SERVICE_NOT_FOUND;
			} else {
				LogUtil.v("是宿主service", info.name);
			}
		}

		return info.name;
	}

	public static void resolveActivity(final Intent intent) {
		// 如果在插件中发现Intent的匹配项，记下匹配的插件Activity的ClassName
		ArrayList<String> classNameList = matchPlugin(intent, PluginDescriptor.ACTIVITY);
		if (classNameList != null && classNameList.size() > 0) {
            //TODO 只取第一个，忽略了多Activity匹配到同一个Intent的情况
			String className = classNameList.get(0);
			PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByClassName(className);

			PluginActivityInfo pluginActivityInfo = pluginDescriptor.getActivityInfos().get(className);

			String stubActivityName = PluginProviderClient.bindStubActivity(className,
					Integer.parseInt(pluginActivityInfo.getLaunchMode()),
					pluginDescriptor.getPackageName(),
					pluginActivityInfo.getTheme(),
                    pluginActivityInfo.getScreenOrientation());

            if (stubActivityName == null) {
                LogUtil.e("绑定StubAtivity失败",
                        className,
                        Integer.parseInt(pluginActivityInfo.getLaunchMode()),
                        pluginDescriptor.getPackageName(),
                        pluginActivityInfo.getTheme(),
                        pluginActivityInfo.getScreenOrientation());
                return;
            } else {
                LogUtil.v("绑定StubAtivity成功", className);
            }

			intent.setComponent(
					new ComponentName(FairyGlobal.getApplication().getPackageName(), stubActivityName));
			//PluginInstrumentationWrapper检测到这个标记后会进行替换
			intent.setAction(className + CLASS_SEPARATOR + (intent.getAction()==null?"":intent.getAction()));
		} else {
			if (intent.getComponent() != null) {
                //如果没有匹配到，但是intent里面指定的packageName是插件的，强行修正packageName
                String targetPackageName = intent.getComponent().getPackageName();
                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(targetPackageName);
                if (pluginDescriptor != null) {
                    intent.setComponent(new ComponentName(FairyGlobal.getApplication().getPackageName(), intent.getComponent().getClassName()));
                }
            }
        }
	}

	/* package */static void resolveActivity(Intent[] intent) {
		// 不常用。需要时再实现此方法，
	}

	/**
	 */
	public static ArrayList<String> matchPlugin(Intent intent, int type) {

        if (intent.getAction() != null && (
                intent.getAction().endsWith(".STUB_DEFAULT") ||
                intent.getAction().endsWith(".STUB_EXACT") ||
                intent.getAction().contains(CLASS_SEPARATOR))) {
            return null;
        }

        LogUtil.v("开始尝试匹配插件Intent");

        ArrayList<String> result = null;

		String packageName = intent.getPackage();
		if (packageName == null && intent.getComponent() != null) {
			packageName = intent.getComponent().getPackageName();
		}
		//如果指定了packname，就不用遍历插件列表了
		if (packageName != null && !packageName.equals(FairyGlobal.getApplication().getPackageName())) {
			PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
			if (pluginDescriptor != null) {
                result = pluginDescriptor.matchPlugin(intent, type);
				if (result != null && result.size() > 0) {
                    LogUtil.v(packageName, "插件Intent匹配成功");
                } else {
                    LogUtil.w(packageName, "目标是插件，但在插件Maniest中未找到匹配的IntentFilter", type);
                }
			} else {
                LogUtil.w(packageName, "目标不是插件，也可能是插件未正确安装");
            }
		} else {
            //没有指定packageName，开始遍历插件列表
            ArrayList<PluginDescriptor> pluginList = PluginManagerHelper.getPlugins();
            LogUtil.v("已安装插件数量", pluginList.size());
            Iterator<PluginDescriptor> itr = pluginList.iterator();
			while (itr.hasNext()) {
                PluginDescriptor pluginDescriptor = itr.next();
                LogUtil.v("正在匹配插件", pluginDescriptor.getPackageName());
                ArrayList<String> list = pluginDescriptor.matchPlugin(intent, type);
				if (list != null) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.addAll(list);
                }
			}

            if (result == null || result.size() == 0) {
                LogUtil.v("未匹配到插件Intent, 说明目标不是插件，也可能是插件未正确安装", packageName, intent.toString());
            } else {
                LogUtil.v(packageName, "插件Intent匹配成功");
            }
		}
		return result;
	}

}
