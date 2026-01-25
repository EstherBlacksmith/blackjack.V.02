package com.itacademy.blackjack.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
@Testcontainers
public class TestMongoConfig {

    @Container
    static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer("mongo:6.0")
                .withExposedPorts(27017);
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Bean
    @Primary
    public MongoClient mongoClient() {
        return MongoClients.create(mongoDBContainer.getReplicaSetUrl("test_blackjack"));
    }

    @Bean
    @Primary
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient) {
        return new ReactiveMongoTemplate(mongoClient, "test_blackjack");
    }
}



