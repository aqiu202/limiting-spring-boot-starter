package com.github.aqiu202.limit.cache.data.str;


import com.github.aqiu202.limit.cache.data.impl.CaffeineCache;
import com.github.aqiu202.limit.cache.data.StringTimeLimitedCache;

public class StringCaffeineCache extends CaffeineCache<String, String> implements
        StringTimeLimitedCache {

    public StringCaffeineCache() {
    }

}
