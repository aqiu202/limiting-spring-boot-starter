package com.redsoft.starters.limit.keygen;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface KeyGenerator {

    /**
     * Generate a key for the given method and its parameters.
     * @param target the target instance
     * @param method the method being called
     * @param params the method parameters (with any var-args expanded)
     * @return a generated key
     */
    String generate(HttpServletRequest request, Object target, Method method, Object... params);

}
