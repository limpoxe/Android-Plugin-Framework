package com.example.plugintestbase;

import java.io.Serializable;

/**
 * Created by cailiming on 16/1/1.
 */
public interface ILoginService extends Serializable {

    public LoginVO login(String username, String password);

    public boolean logout(String username);

}
