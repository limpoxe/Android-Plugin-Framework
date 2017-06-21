package com.limpoxe.fairy.manager;

import com.limpoxe.fairy.content.PluginDescriptor;

/**
 * 插件组件动态绑定到宿主的虚拟stub组件的映射器
 */
public interface StubMappingProcessor {

    public static final int TYPE_ACTIVITY = 1;

    /**
     *
     * @param pluginComponentClassName  插件组件的className，
     * @param componentType  插件组件类型，比如Activity、Service，暂时只支持Activity，其他组件看情况再考虑是否支持
     * @return  插件组件动态绑定的到宿主的虚拟stub组件的className，此虚拟stub组件的className需配置在插件进程。
     *          return null if not matched
     */
    String bindStub(PluginDescriptor pluginDescriptor, String pluginComponentClassName, int componentType);

    /**
     * 解除绑定，如果被绑定的StubClass不能同时被多个插件Class同时绑定，则需要实现此接口，用于解绑，回收StubClass
     * 否则可以忽略
     */
    void unBindStub(String stubClassName, String pluginStubClass, int componentType);
}
