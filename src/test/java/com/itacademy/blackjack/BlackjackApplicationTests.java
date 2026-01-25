package com.itacademy.blackjack;

import com.itacademy.blackjack.config.TestMongoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMongoConfig.class)  // ADD THIS LINE
class BlackjackApplicationTests {
	@Test
	void contextLoads() {
	}
}
