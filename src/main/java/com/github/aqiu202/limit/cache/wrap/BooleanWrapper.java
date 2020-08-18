package com.github.aqiu202.limit.cache.wrap;

import org.springframework.lang.NonNull;

public class BooleanWrapper implements Wrapper<Boolean> {

    private Boolean value = Boolean.FALSE;

    public BooleanWrapper() {

    }

    public BooleanWrapper(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void set(@NonNull Boolean value) {
        this.value = value;
    }

    public void not() {
        this.value = !this.value;
    }

}
