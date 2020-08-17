package com.redsoft.starters.limit.lock;

import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import java.util.concurrent.TimeUnit;

public interface CacheableLock {

    String STRING_VALUE = "1";

    StringTimeLimitedCache getCache();

    Boolean releaseLock(String key, long expired, TimeUnit timeUnit);

    Boolean tryLock(String key, long expires, TimeUnit timeUnit);

    void setTimeout(long timeout);

    void setTimeUnit(TimeUnit timeUnit);
}
