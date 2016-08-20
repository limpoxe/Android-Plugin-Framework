package com.example.plugintest;

import com.example.plugintestbase.ILoginService;
import com.example.plugintestbase.LoginVO;
import com.example.plugintestbase.UserVO;

/**
 * Created by cailiming on 16/1/1.
 */
public class PluginLoginService implements ILoginService {

    @Override
    public LoginVO login(String username, String password) {
        if ("admin".equals(username) && "123456".equals(password)) {
            LoginVO loginVO = new LoginVO();
            loginVO.setUsername(username);
            loginVO.setPassword(password);
            return loginVO;
        }
        return null;
    }

    @Override
    public boolean logout(String username) {
        if ("admin".equals(username)) {
            return true;
        }
        return false;
    }
}
