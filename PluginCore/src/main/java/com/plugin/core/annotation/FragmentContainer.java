package com.plugin.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在宿主程序的activity中标记这个注解，
 * 用来通知插件框架，宿主的这个activity也需要替换上下文，用来嵌入来自插件的fragment
 *
 * 如果是插件自身提供的fragment容器来嵌入来自插件自身的fragment，无需添加此标记。
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FragmentContainer {
    public String pluginId() default "";
    public String fragmentId() default "";
}
