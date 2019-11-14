package com.redsoft.starters.limit.cache;

public interface CatchableLock {

    Boolean unlocked(String key);
}
