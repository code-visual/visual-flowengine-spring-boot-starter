package com.github.managetech.config;

import java.lang.annotation.*;

/**
 * @author Levi Li
 * @since 09/20/2023
 */
//自定义注解@GroovyFunction。用来标识用于绑定到GroovyShell的类。groovy 和java类都可以 这个注解的意义？？
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Documented
public @interface GroovyFunction {
}
