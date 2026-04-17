package com.ecommerce.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility to bridge Spring-managed beans to legacy non-Spring classes (like JavaFX Controllers).
 */
@Component
public class SpringContextBridge implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not initialized.");
        }
        return context.getBean(beanClass);
    }
}
