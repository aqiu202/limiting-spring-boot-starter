package com.redsoft.starters.limit.cache.data.str;


import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import com.redsoft.starters.limit.cache.data.impl.GuavaCache;

public class StringGuavaCache extends GuavaCache<String, String> implements
        StringTimeLimitedCache {

    public StringGuavaCache() {
    }
}
