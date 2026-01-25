package com.itacademy.blackjack;

import com.itacademy.blackjack.config.TestMongoConfig;
import com.itacademy.blackjack.config.TestcontainersInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersInitializer.class)
@SpringBootTest
class BlackjackApplicationTests {
    @Test
    void contextLoads() {
    }
}
