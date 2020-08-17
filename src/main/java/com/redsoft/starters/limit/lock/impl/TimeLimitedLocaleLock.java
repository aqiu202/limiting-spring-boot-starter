package com.redsoft.starters.limit.lock.impl;

import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import com.redsoft.starters.limit.lock.CacheableLock;
import java.util.concurrent.TimeUnit;

/**
 * @author aqiu
 * @date 2020/2/5 4:45 下午
 * @description 集中式缓存实现的锁
 **/
public class TimeLimitedLocaleLock implements CacheableLock {

    private final StringTimeLimitedCache cacheable;

    public TimeLimitedLocaleLock(StringTimeLimitedCache cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public StringTimeLimitedCache getCache() {
        return cacheable;
    }

    @Override
    public Boolean releaseLock(String key, long expired, TimeUnit timeUnit) {
        return this.cacheable.delete(key, expired, timeUnit);
    }

    @Override
    public Boolean tryLock(String key, long expired, TimeUnit timeUnit) {
        return this.cacheable.setIfAbsent(key, STRING_VALUE, expired, timeUnit);
    }

    @Override
    public void setTimeout(long timeout) {
        this.cacheable.setTimeout(timeout);
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.cacheable.setTimeUnit(timeUnit);
    }
}
