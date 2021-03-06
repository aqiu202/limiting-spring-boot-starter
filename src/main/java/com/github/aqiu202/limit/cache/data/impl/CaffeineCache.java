package com.github.aqiu202.limit.cache.data.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.aqiu202.limit.cache.wrap.BooleanWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.springframework.lang.NonNull;

public class CaffeineCache<K, V> extends AbstractTimeLimitedCache<K, V> {

    private final Map<Long, Cache<K, V>> cacheMap = new HashMap<>();

    private Cache<K, V> defaultCache;


    public CaffeineCache() {
    }

    @Override
    public void set(@NonNull K key, @NonNull V value, long expired, @NonNull TimeUnit unit) {
        this.getCache(expired, unit).put(key, value);
    }

    @Override
    public V get(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        return this.getCache(expired, unit).getIfPresent(key);
    }

    @Override
    public Boolean exists(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        return Objects.nonNull(this.get(key, expired, unit));
    }

    @Override
    public Boolean setIfAbsent(@NonNull K key, @NonNull V value, long expired,
            @NonNull TimeUnit unit) {
        BooleanWrapper flag = new BooleanWrapper();
        this.getCache(expired, unit).get(key, (k) -> {
            flag.not();
            return value;
        });
        return flag.get();
    }

    @Override
    public Boolean delete(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        this.getCache(expired, unit).invalidate(key);
        return Boolean.TRUE;
    }

    private Cache<K, V> newCacheInstance(long expired, TimeUnit unit) {
        return Caffeine.newBuilder().initialCapacity(1)
                .expireAfterWrite(expired, unit).build();
    }

    private Cache<K, V> getCache(long expired, TimeUnit timeUnit) {
        if (this.inDefaultCache(expired)) {
            if (Objects.isNull(this.defaultCache)) {
                this.defaultCache = this.newCacheInstance(this.timeout, this.timeUnit);
            }
            return this.defaultCache;
        }
        long key = this.convertToSeconds(expired, timeUnit);
        Cache<K, V> cache = this.cacheMap.get(key);
        if (Objects.isNull(cache)) {
            cache = this.newCacheInstance(expired, timeUnit);
            this.cacheMap.put(key, cache);
        }
        return cache;
    }

}
