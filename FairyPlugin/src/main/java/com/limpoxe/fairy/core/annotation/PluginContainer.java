package com.limpoxe.fairy.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在activity中标记这个注解，
 * 用来通知插件框架，这个activity需要替换上下文，用来嵌入来自其他插件的组件
 * 同时配置了这个注解的Activity需要运行再插件进程中
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface PluginContainer {
    public String pluginId() default "";
}
