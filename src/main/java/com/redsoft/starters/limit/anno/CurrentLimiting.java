package com.redsoft.starters.limit.anno;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.redsoft.starters.limit.keygen.KeyGenerator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface CurrentLimiting {

    /**
     * 每秒可处理的请求数量
     * @return 请求数量
     */
    int permits() default 100;

    String message() default "服务器繁忙，请稍后再试";

    String keyGenerator() default KeyGenerator.DEFAULT_METHOD_KEY_GENERATOR;

    /**
     * key设置
     * @return key
     */
    String key() default "";

}
