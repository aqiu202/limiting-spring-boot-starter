package com.redsoft.starters.limit.keygen.impl;

import com.redsoft.starters.limit.keygen.MethodKeyGenerator;
import java.lang.reflect.Method;
import java.util.StringJoiner;

public class SimpleMethodKeyGenerator implements MethodKeyGenerator {

    @Override
    public String generate(Object target, Method method,
            Object... params) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object param : params) {
            joiner.add(param.getClass().getName());
        }
        return target.getClass().getName().concat(":").concat(method.getName()).concat(":")
                .concat(joiner.toString());
    }
}
