package com.limpoxe.fairy.core;

import dalvik.system.PathClassLoader;

/**
 * 为了支持Receiver和Service，增加此类。
 * 
 * @author Administrator
 * 
 */
public class HostClassLoader extends PathClassLoader {

    public HostClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, parent);
    }

}
