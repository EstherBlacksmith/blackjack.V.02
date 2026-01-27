package com.itacademy.blackjack.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class R2dbcConfig {
//
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        MySqlConnectionConfiguration config = MySqlConnectionConfiguration.builder()
//                .host("localhost")
//                .port(3306)
//                .database("blackjack")
//                .username("blackjack")
//                .password("blackjack123")
//                .build();
//
//        return MySqlConnectionFactory.from(config);
//    }
//}
