package com.redsoft.starters.limit.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.redsoft.starters.limit.cache.CatchableLock;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaCacheLock implements CatchableLock {

    private final Cache<String, String> cache;

    public GuavaCacheLock(int timeout) {
        this.cache = CacheBuilder.newBuilder().initialCapacity(10)
                .expireAfterWrite(timeout, TimeUnit.SECONDS).build();
    }

    @Override
    public Boolean unlocked(String key) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        try {
            return uuid.equals(cache.get(key, () ->
                    uuid
            ));
        } catch (ExecutionException e) {
            return false;
        }
    }
}
