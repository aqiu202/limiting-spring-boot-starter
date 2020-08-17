package com.redsoft.starters.limit.cache.data.str;


import com.redsoft.starters.limit.cache.data.StringTimeLimitedCache;
import com.redsoft.starters.limit.cache.data.impl.CaffeineCache;

public class StringCaffeineCache extends CaffeineCache<String, String> implements
        StringTimeLimitedCache {

    public StringCaffeineCache() {
    }

}
