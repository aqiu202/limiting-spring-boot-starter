package com.redsoft.starters.limit.cache.data.str;

import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import com.redsoft.starters.limit.cache.data.impl.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;

public class StringRedisCache extends RedisCache<String, String> implements
        StringTimeLimitedCache {

    public StringRedisCache(
            StringRedisTemplate cache) {
        super(cache);
    }
}
