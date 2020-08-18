package com.github.aqiu202.limit.cache.wrap;

public interface Wrapper<T> {

    T get();

    void set(T value);
}
