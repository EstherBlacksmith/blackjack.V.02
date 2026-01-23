package com.itacademy.blackjack.game.infrastructure.persistence.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDocument {

    @Id
    private String id;

    // Player information
    private String playerName;
    private List<CardDocument> playerCards;
    private int playerScore;

    // Crupier (dealer) information
    private List<CardDocument> crupierCards;
    private int crupierScore;

    // Game status
    private String gameStatus;
    private String gameResult;

    // Timestamps
    private Instant createdAt;
    private Instant finishedAt;

    /**
     * Nested document for Card representation in MongoDB
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDocument {
        private String rank;
        private String suit;
        private int value;
    }
}
