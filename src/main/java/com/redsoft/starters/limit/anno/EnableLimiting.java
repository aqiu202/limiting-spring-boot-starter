package com.redsoft.starters.limit.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.redsoft.starters.limit.config.CacheConfigRegistrar;
import com.redsoft.starters.limit.config.RepeatLimitConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import({RepeatLimitConfig.class, CacheConfigRegistrar.class})
public @interface EnableLimiting {

    enum CacheMode {
        redis, guava, caffeine
    }

    CacheMode mode() default CacheMode.caffeine;

    /**
     * 多少秒内每个用户只允许访问一次
     */
    int timeout() default 3;

}
