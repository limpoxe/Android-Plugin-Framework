package com.example.plugintest;

import com.example.pluginsharelib.SharePOJO;
import com.example.pluginsharelib.ShareService;
import com.limpoxe.fairy.util.LogUtil;

/**
 * Created by cailiming on 16/5/18.
 */
public class PluginSharedService implements ShareService {

    @Override
    public SharePOJO doSomething(String condition) {
        LogUtil.d(condition);
        return new SharePOJO(condition + " : 插件追加的文字");
    }
}
