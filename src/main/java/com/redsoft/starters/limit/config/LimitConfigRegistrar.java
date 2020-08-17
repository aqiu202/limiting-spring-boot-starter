package com.redsoft.starters.limit.config;


import com.redsoft.starters.limit.anno.EnableLimiting;
import com.redsoft.starters.limit.anno.EnableLimiting.CacheMode;
import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import com.redsoft.starters.limit.cache.data.str.StringCaffeineCache;
import com.redsoft.starters.limit.cache.data.str.StringGuavaCache;
import com.redsoft.starters.limit.lock.impl.RedisTimeLimitedLocaleLock;
import com.redsoft.starters.limit.lock.impl.TimeLimitedLocaleLock;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class LimitConfigRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
            @Nonnull BeanDefinitionRegistry registry) {
        Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(
                EnableLimiting.class.getName(), false);
        if (map == null) {
            return;
        }
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(map);
        long timeout = attributes.getNumber("timeout");
        CacheMode cacheMode = attributes.getEnum("cacheMode");
        TimeUnit timeUnit = attributes.getEnum("timeUnit");
        GenericBeanDefinition b = new GenericBeanDefinition();
        if (CacheMode.redis.equals(cacheMode)) {
            b.setBeanClass(RedisTimeLimitedLocaleLock.class);
            b.setAutowireCandidate(true);
            b.getPropertyValues().add("timeout", timeout);
            b.getPropertyValues().add("timeUnit", timeUnit);
        } else {
            b.setBeanClass(TimeLimitedLocaleLock.class);
            StringTimeLimitedCache stringTimeLimitedCache;
            if (CacheMode.caffeine.equals(cacheMode)) {
                stringTimeLimitedCache = new StringCaffeineCache();
            } else {
                stringTimeLimitedCache = new StringGuavaCache();
            }
            stringTimeLimitedCache.setTimeout(timeout);
            stringTimeLimitedCache.setTimeUnit(timeUnit);
            b.getConstructorArgumentValues()
                    .addIndexedArgumentValue(0, stringTimeLimitedCache);
        }
        registry.registerBeanDefinition("simpleCacheableLock", b);
    }

}
