package com.github.managetech.groovy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
