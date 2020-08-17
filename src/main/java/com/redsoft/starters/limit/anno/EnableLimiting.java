package com.redsoft.starters.limit.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.redsoft.starters.limit.config.LimitConfigRegistrar;
import com.redsoft.starters.limit.config.RepeatLimitConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import({LimitConfigRegistrar.class, RepeatLimitConfig.class})
public @interface EnableLimiting {

    enum CacheMode {
        redis, guava, caffeine
    }

    CacheMode cacheMode() default CacheMode.caffeine;

    /**
     * 多少秒内每个用户只允许访问一次
     */
    long timeout() default 3;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 每秒可处理的请求数量
     */
    int permits() default 100;

    /**
     * 可允许的最大访问线程数量
     */
    int threads() default 200;

}
