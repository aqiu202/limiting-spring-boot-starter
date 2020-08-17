package com.redsoft.starters.limit.lock.impl;

import com.redsoft.starters.limit.cache.data.str.StringRedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <pre>集中式缓存实现的锁</pre>
 * @author aqiu
 **/
public class RedisTimeLimitedLocaleLock extends TimeLimitedLocaleLock {

    public RedisTimeLimitedLocaleLock(StringRedisTemplate stringRedisTemplate) {
        super(new StringRedisCache(stringRedisTemplate));
    }

}
