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
public @interface RepeatLimiting {

    String key() default "";

    String message() default "请不要重复提交";

    String generatorName() default "";
}
