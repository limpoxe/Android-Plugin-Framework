package com.limpoxe.fairy.manager;

/**
 * 插件组件动态绑定到宿主的虚拟stub组件的映射器
 */
public interface StubMappingProcessor {

    public static final int TYPE_ACTIVITY = 1;

    /**
     *
     * @param pluginComponentClassName  插件组件的className，
     * @param componentType  插件组件类型，比如Activity Or Service，暂时指支持Activity
     * @return  插件组件动态绑定的到宿主的虚拟stub组件的className，此虚拟stub组件的className需配置在插件进程。
     *          return null if not matched
     */
    String bindStub(String pluginId, String pluginComponentClassName, int componentType);
}
