package com.redsoft.starters.limit.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.redsoft.starters.limit.cache.CatchableLock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheLock implements CatchableLock {

    private final Cache<String, String> cache;

    public CaffeineCacheLock(int timeout) {
        this.cache = Caffeine.newBuilder().initialCapacity(10)
                .expireAfterWrite(timeout, TimeUnit.SECONDS).build();
    }

    @Override
    public Boolean unlocked(String key) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.equals(cache.get(key, (k) ->
                uuid
        ));
    }

}
