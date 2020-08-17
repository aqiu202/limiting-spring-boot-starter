package com.redsoft.starters.limit.cache.wrap;

public interface Wrapper<T> {

    T get();

    void set(T value);
}
