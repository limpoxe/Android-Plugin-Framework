package com.limpoxe.fairy.manager.mapping;

import com.limpoxe.fairy.content.PluginDescriptor;

/**
 * 插件组件动态绑定到宿主的虚拟stub组件的映射器
 * Manifest中节点下的各种组合比较多，而框架内置的stub有限，若有不同于框架内置的stub需要添加，
 * 则可以通过注册StubMappingProcessor来添加自定义的处理器
 */
public interface StubMappingProcessor {

    public static final int TYPE_ACTIVITY = 1;
    public static final int TYPE_RECEIVER = 2;
    public static final int TYPE_SERVICE = 3;

    /**
     * 组件类型，表面这个处理器可以处理哪些类型的组件的绑定工作
     * @return
     */
    int getType();

    /**
     *
     * @param pluginDescriptor
     * @param pluginComponentClassName 插件组件名
     * @return   返回插件组件绑定到的宿主stub组件名
     */
    String bindStub(PluginDescriptor pluginDescriptor, String pluginComponentClassName);

    /**
     * 解除绑定，如果被绑定的StubClass不能同时被多个插件Class同时绑定，
     * 则需要实现此接口，用于解绑，回收StubClass否则可以忽略
     */
    void unBindStub(String stubClassName, String pluginStubClass);

    /**
     * 判断这个组件是否是一个stub组件
     * @param stubClassName
     * @return
     */
    boolean isStub(String stubClassName);

    /**
     * 反查这个stub和哪个插件组件绑定了
     * @param stubClassName
     * @return 插件组件名
     */
    String getBindedPluginClassName(String stubClassName);

}
