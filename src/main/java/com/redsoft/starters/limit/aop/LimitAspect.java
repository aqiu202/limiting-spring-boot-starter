package com.redsoft.starters.limit.aop;


import com.google.common.util.concurrent.RateLimiter;
import com.redsoft.starters.limit.anno.CurrentLimiting;
import com.redsoft.starters.limit.anno.RepeatLimiting;
import com.redsoft.starters.limit.anno.ThreadLimiting;
import com.redsoft.starters.limit.keygen.KeyGenerator;
import com.redsoft.starters.limit.keygen.impl.SimpleKeyGenerator;
import com.redsoft.starters.limit.lock.CacheableLock;
import com.redsoft.starters.limit.spel.EvaluationFiller;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SuppressWarnings("all")
@Aspect
public class LimitAspect implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KeyGenerator defaultKeyGenerator = new SimpleKeyGenerator();
    private final Map<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    private final CacheableLock cacheableLock;
    private final EvaluationFiller evaluationFiller;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private final ExpressionParser parser = new SpelExpressionParser();

    public LimitAspect(CacheableLock cacheableLock, EvaluationFiller evaluationFiller) {
        this.cacheableLock = cacheableLock;
        this.evaluationFiller = evaluationFiller;
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:19 上午
     * @description 防重复提交（限时锁）-类拦截
     * @param pjp: 切点
     * @return {@link Object}
     **/
    @Around("@within(com.redsoft.starters.limit.anno.RepeatLimiting)")
    public Object repeats(ProceedingJoinPoint pjp) throws Throwable {
        Method m = this.getMethod(pjp);
        RepeatLimiting manno = m.getDeclaredAnnotation(RepeatLimiting.class);
        //方法上有相同注解 跳过类拦截
        if (manno != null) {
            return pjp.proceed();
        }
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        RepeatLimiting anno = c.getDeclaredAnnotation(RepeatLimiting.class);
        return this.repeat(pjp, anno);
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:18 上午
     * @description 防重复提交（限时锁）-方法拦截
     * @param pjp: 切点
     * @param repeatLimiting: 注解信息
     * @return {@link Object}
     **/
    @Around("@annotation(repeatLimiting)")
    public Object repeat(ProceedingJoinPoint pjp, RepeatLimiting repeatLimiting)
            throws Throwable {
        String generatorName = repeatLimiting.keyGenerator();
        RepeatLimiting canno = pjp.getTarget().getClass()
                .getDeclaredAnnotation(RepeatLimiting.class);
        long timeout = repeatLimiting.timeout();
        //如果类上有相同注解 合并注解参数 方法参数优先级更高
        String key = repeatLimiting.key();
        TimeUnit timeUnit = repeatLimiting.timeUnit();
        if (canno != null) {
            if (StringUtils.isEmpty(generatorName)) {
                generatorName = canno.keyGenerator();
            }
            if (timeout <= 0) {
                timeout = canno.timeout();
            }
            if (StringUtils.isEmpty(key)) {
                key = canno.key();
            }
            timeUnit = canno.timeUnit();
        }
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        Object target = pjp.getTarget();
        if (StringUtils.isEmpty(key)) {
            key = this.generatorKey(generatorName, target, m, params);
        }
        key = this.processKey(key, target, m, params);
        Boolean unlocked = cacheableLock.tryLock(key, timeout, timeUnit);
        if (!unlocked) {
            throw new IllegalArgumentException(repeatLimiting.message());
        }
        Throwable error = null;
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            logger.error("", throwable);
            error = throwable;
            throw throwable;
        } finally {
            if (error != null) {
                cacheableLock.releaseLock(key, timeout, timeUnit);
            }
        }
        return result;
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:15 上午
     * @description 并发线程数限流-类拦截
     * @param pjp: 切点
     * @return {@link Object}
     **/
    @Around("@within(com.redsoft.starters.limit.anno.ThreadLimiting)")
    public Object threads(ProceedingJoinPoint pjp) throws Throwable {
        Method m = this.getMethod(pjp);
        ThreadLimiting manno = m.getDeclaredAnnotation(ThreadLimiting.class);
        //方法上有相同注解 跳过类拦截
        if (manno != null) {
            return pjp.proceed();
        }
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        ThreadLimiting anno = c.getDeclaredAnnotation(ThreadLimiting.class);
        return this.thread(pjp, anno);
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:15 上午
     * @description 并发线程数限流-方法拦截
     * @param pjp: 切点
     * @param threadLimiting: 注解信息
     * @return {@link Object}
     **/
    @Around("@annotation(threadLimiting)")
    public Object thread(ProceedingJoinPoint pjp, ThreadLimiting threadLimiting)
            throws Throwable {
        String generatorName = threadLimiting.keyGenerator();
        String key = threadLimiting.key();
        ThreadLimiting canno = pjp.getTarget().getClass()
                .getDeclaredAnnotation(ThreadLimiting.class);
        //如果类上有相同注解 合并注解参数 方法参数优先级更高
        if (canno != null) {
            if (StringUtils.isEmpty(generatorName)) {
                generatorName = canno.keyGenerator();
            }
            if (StringUtils.isEmpty(key)) {
                key = canno.key();
            }
        }
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        Object target = pjp.getTarget();
        key = StringUtils.isEmpty(key) ? this.generatorKey(generatorName, target, m, params) : key;
        key = this.processKey(key, target, m, params);
        Semaphore semaphore;
        if ((semaphore = semaphoreMap.get(key)) == null) {
            semaphore = new Semaphore(threadLimiting.threads(), true);
            semaphoreMap.put(key, semaphore);
        }
        if (!semaphore.tryAcquire()) {
            throw new IllegalArgumentException(threadLimiting.message());
        }
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            semaphore.release();
        }
        return result;
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:16 上午
     * @description 令牌桶算法限流-类拦截
     * @param pjp: 切点
     * @return {@link Object}
     **/
    @Around("@within(com.redsoft.starters.limit.anno.CurrentLimiting)")
    public Object currents(ProceedingJoinPoint pjp) throws Throwable {
        Method m = this.getMethod(pjp);
        CurrentLimiting manno = m.getDeclaredAnnotation(CurrentLimiting.class);
        //方法上有相同注解 跳过类拦截
        if (manno != null) {
            return pjp.proceed();
        }
        Object t = pjp.getTarget();
        Class<?> c = t.getClass();
        CurrentLimiting anno = c.getDeclaredAnnotation(CurrentLimiting.class);
        return this.current(pjp, anno);
    }

    /**
     * @author aqiu
     * @date 2020/2/8 1:20 上午
     * @description 令牌桶算法限流-方法拦截
     * @param pjp: 切点
     * @param currentLimiting: 注解信息
     * @return {@link Object}
     **/
    @Around("@annotation(currentLimiting)")
    public Object current(ProceedingJoinPoint pjp, CurrentLimiting currentLimiting)
            throws Throwable {
        String generatorName = currentLimiting.keyGenerator();
        String key = currentLimiting.key();
        CurrentLimiting canno = pjp.getTarget().getClass()
                .getDeclaredAnnotation(CurrentLimiting.class);
        //如果类上有相同注解 合并注解参数 方法参数优先级更高
        if (canno != null) {
            if (StringUtils.isEmpty(generatorName)) {
                generatorName = canno.keyGenerator();
            }
            if (StringUtils.isEmpty(key)) {
                key = canno.key();
            }
        }
        Method m = this.getMethod(pjp);
        Object[] params = pjp.getArgs();
        Object target = pjp.getTarget();
        key = StringUtils.isEmpty(key) ? this.generatorKey(generatorName, target, m, params) : key;
        key = this.processKey(key, target, m, params);
        RateLimiter limiter;
        if ((limiter = rateLimiterMap.get(key)) == null) {
            limiter = RateLimiter.create(currentLimiting.permits());
            rateLimiterMap.put(key, limiter);
        }
        if (!limiter.tryAcquire()) {
            throw new IllegalArgumentException(currentLimiting.message());
        }
        return pjp.proceed();
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
                keyGenerator = this.applicationContext
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

    private String withSpEl(String key, Object target, Method method, Object[] parameters) {
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null,
                method, parameters, new DefaultParameterNameDiscoverer());
        context.setVariable("_method",
                target.getClass().getName().concat(".").concat(method.getName()));
        if (this.evaluationFiller != null) {
            this.evaluationFiller.fill(context, target, method, parameters);
        }
        return this.parser.parseExpression(key).getValue(context, String.class);
    }

    private String processKey(String key, Object target, Method method, Object[] parameters) {
        if (this.isSpel(key)) {
            key = this.withSpEl(key, target, method, parameters);
        } else {
            key = this.notWithSpel(key);
        }
        return key;
    }

    private boolean isSpel(String key) {
        return key.replace("\\#", "").contains("#");
    }

    private String notWithSpel(String key) {
        return key.replace("\\#", "#");
    }

}
