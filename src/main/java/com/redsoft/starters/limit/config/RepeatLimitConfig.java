package com.redsoft.starters.limit.config;


import com.redsoft.starters.limit.aop.LimitAspect;
import com.redsoft.starters.limit.cache.CatchableLock;
import com.redsoft.starters.limit.util.SpringContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RepeatLimitConfig {

    @Bean
    public LimitAspect noRepeatService(CatchableLock catchableLock) {
        return new LimitAspect(catchableLock);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
}
