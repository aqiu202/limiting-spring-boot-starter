package com.redsoft.starters.limit.cache.data.impl;

import com.redsoft.starters.limit.cache.data.TimeLimitedCache;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTimeLimitedCache<K, V> implements TimeLimitedCache<K, V> {

    protected long timeout = DEFAULT_EXPIRED;

    protected TimeUnit timeUnit = DEFAULT_TIME_UNIT;

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    protected boolean inDefaultCache(long expired) {
        return expired <= 0;
    }

    protected long convertToSeconds(long expired, TimeUnit timeUnit) {
        return TimeUnit.SECONDS.convert(expired, timeUnit);
    }
}
