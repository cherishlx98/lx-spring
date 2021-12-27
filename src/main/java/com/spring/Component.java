package com.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    // 定义的bean名字，如果没有定义bean的名字，spring会将类名首字母小写作为bean的名字
    String value() default "";
}
