package com.example.plugintestbase;

import java.io.Serializable;

/**
 * Created by cailiming on 16/1/1.
 */
public class UserVO implements Serializable {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
