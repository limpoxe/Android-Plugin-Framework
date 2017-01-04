package com.example.plugintest.vo;

import java.io.Serializable;

/**
 * Created by cailiming on 17/1/4.
 */

public class DataBindingTestVO implements Serializable {

    public DataBindingTestVO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;
}
