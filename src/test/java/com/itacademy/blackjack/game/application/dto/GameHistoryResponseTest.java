package com.itacademy.blackjack.game.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameHistoryResponseTest {

    @Test
    void testGameHistoryResponseCreation() {
        // Given & When
        GameHistoryResponse response = new GameHistoryResponse(
                "game-123",
                "2024-01-15T10:30:00Z",
                "PLAYER_WINS",
                21,
                18
        );

        // Then
        assertEquals("game-123", response.gameId());
        assertEquals("2024-01-15T10:30:00Z", response.playedAt());
        assertEquals("PLAYER_WINS", response.result());
        assertEquals(21, response.playerScore());
        assertEquals(18, response.dealerScore());
    }

    @Test
    void testGameHistoryResponseAllResults() {
        // Test all possible game results
        GameHistoryResponse win = new GameHistoryResponse("1", "2024-01-15", "PLAYER_WINS", 21, 19);
        GameHistoryResponse loss = new GameHistoryResponse("2", "2024-01-15", "CRUPIER_WINS", 19, 21);
        GameHistoryResponse push = new GameHistoryResponse("3", "2024-01-15", "PUSH", 20, 20);
        GameHistoryResponse blackjack = new GameHistoryResponse("4", "2024-01-15", "BLACKJACK", 21, 20);

        assertEquals("PLAYER_WINS", win.result());
        assertEquals("CRUPIER_WINS", loss.result());
        assertEquals("PUSH", push.result());
        assertEquals("BLACKJACK", blackjack.result());
    }
}
