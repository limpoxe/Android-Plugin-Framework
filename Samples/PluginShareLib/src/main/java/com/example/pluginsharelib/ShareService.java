package com.example.pluginsharelib;

import java.io.Serializable;

/**
 * Created by cailiming on 16/5/18.
 */
public interface ShareService extends Serializable {

    public SharePOJO doSomething(String condition);

}
