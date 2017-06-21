package com.limpoxe.fairy.manager;

import com.limpoxe.fairy.content.PluginDescriptor;

public class DefaultStubMappingProcessor implements StubMappingProcessor {

    @Override
    public String bindStub(PluginDescriptor pluginDescriptor, String pluginComponentClassName, int componentType) {
        return null;
    }

    @Override
    public void unBindStub(String stubClassName, String pluginStubClass, int componentType) {

    }
}
