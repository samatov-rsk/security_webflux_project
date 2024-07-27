package com.samatov.security.webflux.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Configuration
public class PostgresTestContainerConfig {

    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgresDB.getHost() + ":" + postgresDB.getFirstMappedPort() + "/test");
        registry.add("spring.r2dbc.username", postgresDB::getUsername);
        registry.add("spring.r2dbc.password", postgresDB::getPassword);
        registry.add("spring.flyway.url", () -> "jdbc:postgresql://" + postgresDB.getHost() + ":" + postgresDB.getFirstMappedPort() + "/test");
        registry.add("spring.flyway.user", postgresDB::getUsername);
        registry.add("spring.flyway.password", postgresDB::getPassword);
    }
}
