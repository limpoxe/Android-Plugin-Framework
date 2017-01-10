package com.example.plugintest;

import com.example.pluginsharelib.SharePOJO;
import com.example.pluginsharelib.ShareService;

/**
 * Created by cailiming on 16/5/18.
 */
public class PluginSharedService implements ShareService {

    @Override
    public SharePOJO doSomething(String condition) {
        Log.d(condition);
        return new SharePOJO(condition + " : 插件追加的文字");
    }
}
