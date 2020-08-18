package com.github.aqiu202.limit.config;


import com.github.aqiu202.limit.aop.LimitAspect;
import com.github.aqiu202.limit.keygen.KeyGenerator;
import com.github.aqiu202.limit.lock.CacheableLock;
import com.github.aqiu202.limit.spel.EvaluationFiller;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RepeatLimitConfig {

    @Bean
    @ConditionalOnMissingBean
    public LimitAspect limitingAspectService(CacheableLock cacheableLock,
            @Autowired(required = false) EvaluationFiller evaluationFiller) {
        return new LimitAspect(cacheableLock, evaluationFiller);
    }

    @Bean(name = KeyGenerator.DEFAULT_METHOD_KEY_GENERATOR)
    @ConditionalOnMissingBean(value = KeyGenerator.class, name = KeyGenerator.DEFAULT_METHOD_KEY_GENERATOR)
    public KeyGenerator methodKeyGenerator() {
        return (request, target, method, params) -> {
            StringJoiner joiner = new StringJoiner(",");
            for (Object param : params) {
                joiner.add(param.getClass().getName());
            }
            return target.getClass().getName().concat(":").concat(method.getName()).concat(":")
                    .concat(joiner.toString());
        };
    }

}
