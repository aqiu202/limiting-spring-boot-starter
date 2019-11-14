package com.redsoft.starters.limit.cache.impl;

import com.redsoft.starters.limit.cache.CatchableLock;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisCacheLock implements CatchableLock {

    private final int timeout;
    private final StringRedisTemplate cache;
    private final static String STRING_VALUE = "1";

    public RedisCacheLock(StringRedisTemplate cache, int timeout) {
        this.timeout = timeout;
        this.cache = cache;
    }

    @Override
    public Boolean unlocked(String key) {
        return cache.opsForValue()
                .setIfAbsent(key, STRING_VALUE, this.timeout, TimeUnit.SECONDS);
    }

}
