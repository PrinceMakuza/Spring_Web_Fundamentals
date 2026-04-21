package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/graphiql").setViewName("forward:/graphiql.html");
        registry.addViewController("/graphiql/").setViewName("forward:/graphiql.html");
        // Redirect root to graphiql for easy access
        registry.addRedirectViewController("/", "/graphiql");
    }
}