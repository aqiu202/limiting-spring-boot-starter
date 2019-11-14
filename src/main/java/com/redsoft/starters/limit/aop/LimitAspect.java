package com.redsoft.starters.limit.aop;


import com.google.common.util.concurrent.RateLimiter;
import com.redsoft.starters.limit.anno.CurrentLimiting;
import com.redsoft.starters.limit.anno.RepeatLimiting;
import com.redsoft.starters.limit.anno.ThreadLimiting;
import com.redsoft.starters.limit.cache.CatchableLock;
import com.redsoft.starters.limit.keygen.KeyGenerator;
import com.redsoft.starters.limit.keygen.MethodKeyGenerator;
import com.redsoft.starters.limit.keygen.impl.SimpleKeyGenerator;
import com.redsoft.starters.limit.keygen.impl.SimpleMethodKeyGenerator;
import com.redsoft.starters.limit.util.SpringContextHolder;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SuppressWarnings("all")
@Aspect
public class LimitAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KeyGenerator defaultKeyGenerator = new SimpleKeyGenerator();
    private final MethodKeyGenerator methodKeyGenerator = new SimpleMethodKeyGenerator();
    private final Map<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    private final CatchableLock catchableLock;

    public LimitAspect(CatchableLock catchableLock) {
        this.catchableLock = catchableLock;
    }

    /**
     * 拦截器具体实现
     *
     * @throws Throwable 异常
     */
    @Around("@within(com.redsoft.starters.limit.anno.RepeatLimiting)") // 指定拦截器规则
    public Object repeats(ProceedingJoinPoint pjp) throws Throwable {
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        RepeatLimiting anno = c.getDeclaredAnnotation(RepeatLimiting.class);
        return this.repeat(pjp, anno);
    }

    @Around("@annotation(repeatLimiting)") // 指定拦截器规则
    public Object repeat(ProceedingJoinPoint pjp, RepeatLimiting repeatLimiting)
            throws Throwable {
        Object result;
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        String key = this.generatorKey(repeatLimiting.generatorName(), pjp.getTarget(), m, params);
        if (!catchableLock.unlocked(key)) {
            throw new IllegalArgumentException(repeatLimiting.message());
        }
        result = pjp.proceed(params);
        return result;
    }

    /**
     * 拦截器具体实现
     *
     * @throws Throwable 异常
     */
    @Around("@within(com.redsoft.starters.limit.anno.ThreadLimiting)") // 指定拦截器规则
    public Object threads(ProceedingJoinPoint pjp) throws Throwable {
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        ThreadLimiting anno = c.getDeclaredAnnotation(ThreadLimiting.class);
        return this.thread(pjp, anno);
    }

    @Around("@annotation(threadLimiting)") // 指定拦截器规则
    public Object thread(ProceedingJoinPoint pjp, ThreadLimiting threadLimiting)
            throws Throwable {
        Object result;
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        String key = this
                .generatorKeyByMethod(threadLimiting.generatorName(), pjp.getTarget(), m, params);
        Semaphore semaphore;
        if ((semaphore = semaphoreMap.get(key)) == null) {
            semaphore = new Semaphore(threadLimiting.threads(), true);
            semaphoreMap.put(key, semaphore);
        }
        if (!semaphore.tryAcquire()) {
            throw new IllegalArgumentException(threadLimiting.message());
        }
        try {
            result = pjp.proceed(params);
        } finally {
            semaphore.release();
        }
        return result;
    }

    /**
     * 拦截器具体实现
     *
     * @throws Throwable 异常
     */
    @Around("@within(com.redsoft.starters.limit.anno.CurrentLimiting)") // 指定拦截器规则
    public Object currents(ProceedingJoinPoint pjp) throws Throwable {
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        CurrentLimiting anno = c.getDeclaredAnnotation(CurrentLimiting.class);
        return this.current(pjp, anno);
    }

    @Around("@annotation(currentLimiting)") // 指定拦截器规则
    public Object current(ProceedingJoinPoint pjp, CurrentLimiting currentLimiting)
            throws Throwable {
        Object result;
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        String key = this
                .generatorKeyByMethod(currentLimiting.generatorName(), pjp.getTarget(), m, params);
        RateLimiter limiter;
        if ((limiter = rateLimiterMap.get(key)) == null) {
            limiter = RateLimiter.create(currentLimiting.permits());
            rateLimiterMap.put(key, limiter);
        }
        if (!limiter.tryAcquire()) {
            throw new IllegalArgumentException(currentLimiting.message());
        }
        result = pjp.proceed(params);
        return result;
    }

    private String generatorKey(String keyGeneratorName, Object target, Method m,
            Object[] params) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request =
                servletRequestAttributes == null ? null : servletRequestAttributes.getRequest();
        String key;
        if (StringUtils.isEmpty(keyGeneratorName)) {
            key = this.defaultKeyGenerator.generate(request, target, m, params);
        } else {
            KeyGenerator keyGenerator;
            try {
                keyGenerator = SpringContextHolder
                        .getBean(keyGeneratorName, KeyGenerator.class);
            } catch (Exception e) {
                logger.error("KeyGenerator 配置错误，没有找到名称为" + keyGeneratorName + "的KeyGenerator：",
                        e);
                keyGenerator = this.defaultKeyGenerator;
            }
            key = keyGenerator.generate(request, target, m, params);
        }
        return key;
    }

    private String generatorKeyByMethod(String keyGeneratorName, Object target, Method m,
            Object[] params) {
        String key;
        if (StringUtils.isEmpty(keyGeneratorName)) {
            key = this.methodKeyGenerator.generate(target, m, params);
        } else {
            MethodKeyGenerator keyGenerator;
            try {
                keyGenerator = SpringContextHolder
                        .getBean(keyGeneratorName, MethodKeyGenerator.class);
            } catch (Exception e) {
                logger.error("MethodKeyGenerator 配置错误，没有找到名称为" + keyGeneratorName + "的MethodKeyGenerator：",
                        e);
                keyGenerator = this.methodKeyGenerator;
            }
            key = keyGenerator.generate(target, m, params);
        }
        return key;
    }

    /**
     * @name getMethod
     * @author AQIU
     * @description 根据aop切面获取当前执行的方法
     * @date 2018/8/8 上午11:16
     * @param pjp pjp
     * @return java.lang.reflect.Method
     **/
    private Method getMethod(ProceedingJoinPoint pjp) {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }
}
