package com.limpoxe.fairy.manager.mapping;

import android.text.TextUtils;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.manager.PluginManagerHelper;

import java.util.ArrayList;

/**
 * 插件组件动态绑定到宿主的虚拟stub组件
 */
public class PluginStubBinding {

    public static String buildDefaultAction() {
        return FairyGlobal.getApplication().getPackageName() + ".STUB_DEFAULT";
    }

	public static synchronized String bindStub(String pluginClassName, String packageName, int type) {
        ArrayList<StubMappingProcessor> list = FairyGlobal.getStubMappingProcessors();
        if(list != null) {
            for(int i = list.size() - 1; i >= 0; i--) {
                StubMappingProcessor processor = list.get(i);
                if (processor.getType() == type) {
                    PluginDescriptor pluginDescriptor = null;
                    if (!TextUtils.isEmpty(packageName)) {
                        pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                    }
                    String stubClass = processor.bindStub(pluginDescriptor, pluginClassName);
                    if (!TextUtils.isEmpty(stubClass)) {
                        return stubClass;
                    }
                }
            }
        }
        return null;
	}

	public static synchronized void unBind(String stubClassName, String pluginClassName, int type) {
        ArrayList<StubMappingProcessor> list = FairyGlobal.getStubMappingProcessors();
        if(list != null) {
            for(int i = list.size() - 1; i >= 0; i--) {
                StubMappingProcessor processor = list.get(i);
                if (processor.getType() == type) {
                    processor.unBindStub(stubClassName, pluginClassName);
                }
            }
        }
	}

	public static synchronized String getBindedPluginClassName(String stubClassName, int type) {
        ArrayList<StubMappingProcessor> list = FairyGlobal.getStubMappingProcessors();
        if(list != null) {
            for(int i = list.size() - 1; i >= 0; i--) {
                StubMappingProcessor processor = list.get(i);
                if (processor.getType() == type) {
                    String bindedClass = processor.getBindedPluginClassName(stubClassName);
                    if (!TextUtils.isEmpty(bindedClass)) {
                        return bindedClass;
                    }
                }
            }
        }
        return null;
	}

	public static boolean isStub(String className) {
        ArrayList<StubMappingProcessor> list = FairyGlobal.getStubMappingProcessors();
        if(list != null) {
            for(int i = list.size() - 1; i >= 0; i--) {
                StubMappingProcessor processor = list.get(i);
                if (processor.isStub(className)) {
                    return true;
                }
            }
        }
        return StubExact.isExact(className, PluginDescriptor.UNKOWN);
	}

}
