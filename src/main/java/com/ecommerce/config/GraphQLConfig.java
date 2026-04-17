package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.context.annotation.Bean;

/**
 * GraphQL configuration. REST and GraphQL coexist on the same port.
 * Spring Boot auto-configures GraphQL at /graphql and GraphiQL at /graphiql.
 */
@Configuration
public class GraphQLConfig {

    // Spring Boot auto-configures GraphQL with the schema.graphqls file
    // and the @Controller annotated resolver beans. No additional
    // RuntimeWiringConfigurer is needed for standard type mappings.
}
