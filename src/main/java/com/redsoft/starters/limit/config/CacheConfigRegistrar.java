package com.redsoft.starters.limit.config;


import com.redsoft.starters.limit.anno.EnableLimiting;
import com.redsoft.starters.limit.anno.EnableLimiting.CacheMode;
import com.redsoft.starters.limit.cache.impl.CaffeineCacheLock;
import com.redsoft.starters.limit.cache.impl.GuavaCacheLock;
import com.redsoft.starters.limit.cache.impl.RedisCacheLock;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class CacheConfigRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(
                EnableLimiting.class.getName());
        int timeout = (int) map.get("timeout");
        GenericBeanDefinition b = new GenericBeanDefinition();
        if (CacheMode.redis.equals(map.get("mode"))) {
            b.setBeanClass(RedisCacheLock.class);
            b.setAutowireCandidate(true);
            b.getConstructorArgumentValues().addIndexedArgumentValue(1, timeout);
        } else if (CacheMode.caffeine.equals(map.get("mode"))) {
            b.setBeanClass(CaffeineCacheLock.class);
            b.getConstructorArgumentValues().addIndexedArgumentValue(0, timeout);
        } else if (CacheMode.guava.equals(map.get("mode"))) {
            b.setBeanClass(GuavaCacheLock.class);
            b.getConstructorArgumentValues().addIndexedArgumentValue(0, timeout);
        }
        registry.registerBeanDefinition("repeatLimitCache", b);
    }

}
