package com.itacademy.blackjack.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
@TestConfiguration
public class TestcontainersInitializer {
    // Use existing Docker containers instead of starting new ones
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0")
            .withAccessToHost(true);

    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("blackjack")
            .withUsername("blackjack")
            .withPassword("blackjack123");

    static {
        // DON'T start new containers - they already exist
        // Just configure properties to point to existing containers
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/blackjack");
        registry.add("spring.r2dbc.url", () -> "r2dbc:mysql://localhost:3306/blackjack");
        registry.add("spring.r2dbc.username", () -> "blackjack");
        registry.add("spring.r2dbc.password", () -> "blackjack123");
    }
}
