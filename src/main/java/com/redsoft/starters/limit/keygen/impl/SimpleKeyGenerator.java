package com.redsoft.starters.limit.keygen.impl;


import com.redsoft.starters.limit.keygen.KeyGenerator;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

public class SimpleKeyGenerator implements KeyGenerator {

    @Override
    public String generate(HttpServletRequest request, Object target, Method method,
            Object... params) {
        String sessionId = request.getSession().getId();
        String fullName = target.getClass().getName() + "." + method.getName();
        return fullName.concat(":").concat(sessionId);
    }

}
