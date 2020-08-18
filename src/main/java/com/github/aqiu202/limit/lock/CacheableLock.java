package com.github.aqiu202.limit.lock;

import com.github.aqiu202.limit.cache.data.StringTimeLimitedCache;
import java.util.concurrent.TimeUnit;

public interface CacheableLock {

    String STRING_VALUE = "1";

    StringTimeLimitedCache getCache();

    Boolean releaseLock(String key, long expired, TimeUnit timeUnit);

    Boolean tryLock(String key, long expires, TimeUnit timeUnit);

    void setTimeout(long timeout);

    void setTimeUnit(TimeUnit timeUnit);
}
