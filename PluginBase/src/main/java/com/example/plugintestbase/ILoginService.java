package com.example.plugintestbase;

/**
 * Created by cailiming on 16/1/1.
 */
public interface ILoginService {

    public LoginVO login(String username, String password);

    public boolean logout(String username);

}
