package com.github.aqiu202.limit.cache.data.str;


import com.github.aqiu202.limit.cache.data.impl.GuavaCache;
import com.github.aqiu202.limit.cache.data.StringTimeLimitedCache;

public class StringGuavaCache extends GuavaCache<String, String> implements
        StringTimeLimitedCache {

    public StringGuavaCache() {
    }
}
