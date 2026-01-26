package com.itacademy.blackjack.player.application.dto;

import com.itacademy.blackjack.game.application.dto.GameHistoryResponse;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStatsResponseTest {

    @Test
    void testPlayerStatsResponseCreation() {
        // Given
        List<GameHistoryResponse> recentGames = List.of(
                new GameHistoryResponse("1", "2024-01-15T10:30:00", "PLAYER_WINS", 21, 18),
                new GameHistoryResponse("2", "2024-01-15T11:00:00", "CRUPIER_WINS", 19, 21)
        );

        // When
        PlayerStatsResponse response = new PlayerStatsResponse(
                10, 5, 3, 2, 50.0, 2, recentGames
        );

        // Then
        assertEquals(10, response.totalGames());
        assertEquals(5, response.wins());
        assertEquals(3, response.losses());
        assertEquals(2, response.pushes());
        assertEquals(50.0, response.winRate());
        assertEquals(2, response.currentStreak());
        assertEquals(2, response.recentGames().size());
    }

    @Test
    void testPlayerStatsResponseWithEmptyGames() {
        // Given & When
        PlayerStatsResponse response = new PlayerStatsResponse(
                0, 0, 0, 0, 0.0, 0, List.of()
        );

        // Then
        assertEquals(0, response.totalGames());
        assertTrue(response.recentGames().isEmpty());
    }
}
