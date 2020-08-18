package com.github.aqiu202.limit.cache.data.impl;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

public class RedisCache<K, V> extends AbstractTimeLimitedCache<K, V> {

    private final RedisTemplate<K, V> cache;

    public RedisCache(@NonNull RedisTemplate<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public void set(@NonNull K key, @NonNull V value, long expired, @NonNull TimeUnit unit) {
        if (this.inDefaultCache(expired)) {
            this.cache.opsForValue().set(key, value, this.timeout, this.timeUnit);
        } else {
            this.cache.opsForValue().set(key, value, expired, unit);
        }
    }

    @Override
    public V get(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        return this.cache.opsForValue().get(key);
    }

    @Override
    public Boolean exists(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        return Objects.isNull(this.get(key, expired, unit));
    }

    @Override
    public Boolean setIfAbsent(@NonNull K key, @NonNull V value, long expired,
            @NonNull TimeUnit unit) {
        if (this.inDefaultCache(expired)) {
            return this.cache.opsForValue().setIfAbsent(key, value, this.timeout, this.timeUnit);
        } else {
            return this.cache.opsForValue().setIfAbsent(key, value, expired, unit);
        }
    }

    @Override
    public Boolean delete(@Nonnull K key, long expired, @Nonnull TimeUnit unit) {
        return this.cache.delete(key);
    }

}
