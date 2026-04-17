package com.ecommerce.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for performance monitoring on repository methods.
 * Uses @Around advice to measure execution time and log warnings
 * for calls exceeding 500ms threshold.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_THRESHOLD_MS = 500;

    /**
     * Pointcut targeting all methods in repository classes.
     */
    @Pointcut("execution(* com.ecommerce.repository.*.*(..))")
    public void repositoryMethods() {}

    /**
     * @Around advice: captures execution time of repository methods.
     * Logs a warning if the method execution exceeds 500ms.
     */
    @Around("repositoryMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > SLOW_THRESHOLD_MS) {
                logger.warn("[PERF-WARNING] SLOW QUERY: {} took {}ms (threshold: {}ms)",
                        methodName, executionTime, SLOW_THRESHOLD_MS);
            } else {
                logger.info("[PERF-MONITOR] {} completed in {}ms", methodName, executionTime);
            }

            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[PERF-ERROR] {} failed after {}ms: {}", methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }
}
