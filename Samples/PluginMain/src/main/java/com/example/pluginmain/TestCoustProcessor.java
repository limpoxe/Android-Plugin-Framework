package com.example.pluginmain;

import com.limpoxe.fairy.manager.mapping.StubActivityExactMappingProcessor;

public class TestCoustProcessor extends StubActivityExactMappingProcessor {

    @Override
    public String getStubActivityName() {
        return "com.example.pluginmain.stub.XXXX";
    }

    @Override
    public String getPluginActivityName() {
        return "com.example.plugintest.activity.CustomMappingActivity";
    }

}
