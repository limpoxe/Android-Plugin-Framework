package com.limpoxe.fairy.manager.mapping;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;

import java.util.List;

import static com.limpoxe.fairy.manager.mapping.PluginStubBinding.buildDefaultAction;

public class StubReceiverMappingProcessor implements StubMappingProcessor {

    private static boolean isPoolInited = false;
    private static String receiver = null;

    @Override
    public String bindStub(PluginDescriptor pluginDescriptor, String pluginReceiverClassName) {

        if (pluginReceiverClassName != null) {
            if (StubExact.isExact(pluginReceiverClassName, PluginDescriptor.BROADCAST)) {
                return pluginReceiverClassName;
            }
        }

        initStubPool();

        return receiver;
    }

    @Override
    public void unBindStub(String stubClassName, String pluginStubClass) {
        //not need
    }

    @Override
    public boolean isStub(String stubClassName) {
        initStubPool();

        return stubClassName.equals(receiver);
    }

    @Override
    public String getBindedPluginClassName(String stubClassName) {
        //not need
        return null;
    }

    @Override
    public int getType() {
        return TYPE_RECEIVER;
    }

    private static void loadStubReceiver() {
        Intent exactStub = new Intent();
        exactStub.setAction(buildDefaultAction());
        exactStub.setPackage(FairyGlobal.getApplication().getPackageName());

        List<ResolveInfo> resolveInfos = FairyGlobal.getApplication().getPackageManager().queryBroadcastReceivers(exactStub, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfos != null && resolveInfos.size() >0) {
            //框架只内置了1个receiver，所以这里直接就get(0)了
            receiver = resolveInfos.get(0).activityInfo.name;
        }

    }

    private static void initStubPool() {

        if (isPoolInited) {
            return;
        }

        loadStubReceiver();

        isPoolInited = true;
    }

}
