package com.github.aqiu202.limit.cache.data;

import java.util.concurrent.TimeUnit;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface TimeLimitedCache<K, V> {

    long DEFAULT_EXPIRED = 3600;

    TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    void set(@NonNull K key, @NonNull V value, long expired, @NonNull TimeUnit unit);

    V get(@NonNull K key, long expired, @NonNull TimeUnit unit);

    Boolean exists(@NonNull K key, long expired, @NonNull TimeUnit unit);

    @Nullable
    Boolean setIfAbsent(@NonNull K key, @NonNull V value, long expired, @NonNull TimeUnit unit);

    Boolean delete(@NonNull K key, long expired, @NonNull TimeUnit unit);

    long getTimeout();

    TimeUnit getTimeUnit();

    void setTimeout(long timeout);

    void setTimeUnit(TimeUnit timeUnit);

}
