package com.redsoft.starters.limit.spel;

import java.lang.reflect.Method;
import org.springframework.expression.EvaluationContext;

@FunctionalInterface
public interface EvaluationFiller {

    void fill(EvaluationContext context, Object target, Method method, Object[] parameters);
}
