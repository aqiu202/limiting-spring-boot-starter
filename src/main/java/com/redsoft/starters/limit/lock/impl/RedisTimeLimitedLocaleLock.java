package com.redsoft.starters.limit.lock.impl;

import com.redsoft.starters.limit.cache.data.str.StringRedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author aqiu
 * @date 2020/2/5 4:45 下午
 * @description 集中式缓存实现的锁
 **/
public class RedisTimeLimitedLocaleLock extends TimeLimitedLocaleLock {

    public RedisTimeLimitedLocaleLock(StringRedisTemplate stringRedisTemplate) {
        super(new StringRedisCache(stringRedisTemplate));
    }

}
