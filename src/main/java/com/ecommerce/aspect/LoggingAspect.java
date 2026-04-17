package com.ecommerce.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect for logging all service method invocations.
 * Captures method name, arguments, and total execution time.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut targeting all methods in service classes.
     */
    @Pointcut("execution(* com.ecommerce.service.*.*(..))")
    public void serviceMethods() {}

    /**
     * @Around advice: logs method entry, arguments, and total execution time.
     */
    @Around("serviceMethods()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());

        logger.info("[AOP-LOG] ENTERING: {} with arguments: {}", methodName, args);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("[AOP-LOG] EXITING: {} (Execution Time: {}ms)", methodName, executionTime);
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[AOP-LOG] EXCEPTION in {} after {}ms: {}", methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }
}
