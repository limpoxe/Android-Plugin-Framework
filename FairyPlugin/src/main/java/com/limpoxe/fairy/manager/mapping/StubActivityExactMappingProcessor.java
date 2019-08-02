package com.limpoxe.fairy.manager.mapping;

import android.util.Pair;

import com.limpoxe.fairy.content.PluginDescriptor;

public abstract class StubActivityExactMappingProcessor implements StubMappingProcessor {

    private Pair<String, String> pair;

    @Override
    final public int getType() {
        return TYPE_ACTIVITY;
    }

    @Override
    final public String bindStub(PluginDescriptor pluginDescriptor, String pluginComponentClassName) {
        if (pluginComponentClassName.equals(getPluginActivityName())) {
            pair = new Pair<>(getStubActivityName(), pluginComponentClassName);
            return pair.first;
        }
        return null;
    }

    @Override
    final public void unBindStub(String stubClassName, String pluginStubClass) {
        if (pair != null && pair.first.equals(stubClassName) && pair.second.equals(pluginStubClass)) {
            pair = null;
        }
    }

    @Override
    final public boolean isStub(String stubClassName) {
        return stubClassName.equals(getStubActivityName());
    }

    @Override
    final public String getBindedPluginClassName(String stubClassName) {
        if (pair != null && pair.first.equals(stubClassName)) {
            return pair.second;
        }
        return null;
    }

    /**
     * @return 在宿主manifest中配置的stub
     */
    public abstract String getStubActivityName();

    /**
     * @return 插件中的组件名称
     */
    public abstract String getPluginActivityName();
}
