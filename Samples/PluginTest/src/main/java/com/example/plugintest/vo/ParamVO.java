package com.example.plugintest.vo;

import java.io.Serializable;

/**
 * Created by cailiming on 15/9/22.
 */
public class ParamVO implements Serializable {
    public String name;

    @Override
    public String toString() {
        return "name:" + name;
    }
}
