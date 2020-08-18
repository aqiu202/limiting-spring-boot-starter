package com.github.aqiu202.limit.cache.data.str;

import com.github.aqiu202.limit.cache.data.impl.RedisCache;
import com.github.aqiu202.limit.cache.data.StringTimeLimitedCache;
import org.springframework.data.redis.core.StringRedisTemplate;

public class StringRedisCache extends RedisCache<String, String> implements
        StringTimeLimitedCache {

    public StringRedisCache(
            StringRedisTemplate cache) {
        super(cache);
    }
}
