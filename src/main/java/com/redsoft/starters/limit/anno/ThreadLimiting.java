package com.redsoft.starters.limit.anno;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface ThreadLimiting {

    int threads() default 200;

    String message() default "服务器繁忙，请稍后再试";

    String key() default "";

    String generatorName() default "";
}
