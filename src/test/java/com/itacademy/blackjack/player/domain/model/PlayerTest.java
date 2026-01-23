package com.itacademy.blackjack.player.domain.model;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.GameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
        player = new Player("TestPlayer", scoringService);
    }

    @Nested
    @DisplayName("applyGameResult() tests")
    class ApplyGameResultTests {

        @Test
        @DisplayName("PLAYER_WINS should increment wins count")
        void playerWins_increasesWinsCount() {
            // Given
            assertEquals(0, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());

            // When
            player.applyGameResult(GameResult.PLAYER_WINS);

            // Then
            assertEquals(1, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());
        }

        @Test
        @DisplayName("BLACKJACK should increment wins count")
        void blackjack_increasesWinsCount() {
            // Given
            assertEquals(0, player.getWins());

            // When
            player.applyGameResult(GameResult.BLACKJACK);

            // Then
            assertEquals(1, player.getWins());
        }

        @Test
        @DisplayName("CRUPIER_WINS should increment losses count")
        void crupierWins_increasesLossesCount() {
            // Given
            assertEquals(0, player.getLosses());

            // When
            player.applyGameResult(GameResult.CRUPIER_WINS);

            // Then
            assertEquals(1, player.getLosses());
            assertEquals(0, player.getWins());
            assertEquals(0, player.getPushes());
        }

        @Test
        @DisplayName("PUSH should increment pushes count")
        void push_increasesPushesCount() {
            // Given
            assertEquals(0, player.getPushes());

            // When
            player.applyGameResult(GameResult.PUSH);

            // Then
            assertEquals(1, player.getPushes());
            assertEquals(0, player.getWins());
            assertEquals(0, player.getLosses());
        }

        @Test
        @DisplayName("NO_RESULTS_YET should not change any statistics")
        void noResultsYet_doesNotChangeStatistics() {
            // Given
            player.applyGameResult(GameResult.PLAYER_WINS);
            assertEquals(1, player.getWins());

            // When
            player.applyGameResult(GameResult.NO_RESULTS_YET);

            // Then
            assertEquals(1, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());
        }

        @Test
        @DisplayName("Multiple game results should accumulate correctly")
        void multipleResults_accumulateCorrectly() {
            // When - Simulate a series of games
            player.applyGameResult(GameResult.PLAYER_WINS);      // 1 win
            player.applyGameResult(GameResult.CRUPIER_WINS);     // 1 loss
            player.applyGameResult(GameResult.PLAYER_WINS);      // 2 wins
            player.applyGameResult(GameResult.PUSH);             // 1 push
            player.applyGameResult(GameResult.BLACKJACK);        // 3 wins
            player.applyGameResult(GameResult.CRUPIER_WINS);     // 2 losses

            // Then
            assertEquals(3, player.getWins());
            assertEquals(2, player.getLosses());
            assertEquals(1, player.getPushes());
        }

        @Test
        @DisplayName("Statistics should be independent for different players")
        void differentPlayers_haveIndependentStatistics() {
            // Given
            Player player2 = new Player("Player2", scoringService);

            // When
            player.applyGameResult(GameResult.PLAYER_WINS);
            player2.applyGameResult(GameResult.CRUPIER_WINS);
            player2.applyGameResult(GameResult.PUSH);

            // Then - Player 1
            assertEquals(1, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());

            // Then - Player 2 (independent)
            assertEquals(0, player2.getWins());
            assertEquals(1, player2.getLosses());
            assertEquals(1, player2.getPushes());
        }
    }

    @Nested
    @DisplayName("Initial state tests")
    class InitialStateTests {

        @Test
        @DisplayName("New player should have zero statistics")
        void newPlayer_hasZeroStatistics() {
            assertEquals(0, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());
        }
    }

    @Nested
    @DisplayName("Unhappy path tests")
    class UnhappyPathTests {

        @Test
        @DisplayName("applyGameResult with null should throw NullPointerException")
        void applyGameResult_withNull_throwsException() {
            // Given
            assertNotNull(player.getWins());

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                player.applyGameResult(null);
            });
        }

        @Test
        @DisplayName("Statistics should remain consistent after exception")
        void statistics_consistentAfterException() {
            // Given - Player has some wins
            player.applyGameResult(GameResult.PLAYER_WINS);
            player.applyGameResult(GameResult.PLAYER_WINS);
            assertEquals(2, player.getWins());

            // When - Try to apply null (should fail)
            try {
                player.applyGameResult(null);
            } catch (NullPointerException e) {
                // Expected
            }

            // Then - Statistics should be unchanged
            assertEquals(2, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());
        }

        @Test
        @DisplayName("Statistics should not go negative with valid results")
        void statistics_doNotGoNegative() {
            // Given - New player has zero statistics
            assertEquals(0, player.getWins());
            assertEquals(0, player.getLosses());
            assertEquals(0, player.getPushes());

            // When - Apply losses multiple times (simulating edge case)
            player.applyGameResult(GameResult.CRUPIER_WINS);
            player.applyGameResult(GameResult.CRUPIER_WINS);
            player.applyGameResult(GameResult.CRUPIER_WINS);

            // Then - Should be positive, not negative
            assertEquals(3, player.getLosses());
            assertTrue(player.getLosses() >= 0);
        }

        @Test
        @DisplayName("Player state should remain valid after many game results")
        void playerState_validAfterManyResults() {
            // When - Simulate many games (stress test)
            for (int i = 0; i < 100; i++) {
                player.applyGameResult(GameResult.PLAYER_WINS);
                player.applyGameResult(GameResult.CRUPIER_WINS);
                player.applyGameResult(GameResult.PUSH);
            }

            // Then - All counts should be positive and consistent
            assertEquals(100, player.getWins());
            assertEquals(100, player.getLosses());
            assertEquals(100, player.getPushes());

            // Total games should match sum
            int totalGames = player.getWins() + player.getLosses() + player.getPushes();
            assertEquals(300, totalGames);
        }

        @Test
        @DisplayName("Player can continue playing after many games")
        void player_canContinueAfterManyGames() {
            // Given - Player has played many games
            for (int i = 0; i < 50; i++) {
                player.applyGameResult(GameResult.PLAYER_WINS);
            }

            // When - Add more results
            player.applyGameResult(GameResult.BLACKJACK);
            player.applyGameResult(GameResult.CRUPIER_WINS);
            player.applyGameResult(GameResult.PUSH);

            // Then - Should accumulate correctly
            assertEquals(51, player.getWins()); // 50 + 1 (BLACKJACK)
            assertEquals(1, player.getLosses());
            assertEquals(1, player.getPushes());
        }

        @Test
        @DisplayName("Player name should not affect statistics")
        void statistics_independentOfPlayerName() {
            // Given - Two players with different names
            Player player1 = new Player("ShortName", scoringService);
            Player player2 = new Player("VeryLongPlayerNameForTesting", scoringService);

            // When - Apply same results
            for (int i = 0; i < 5; i++) {
                player1.applyGameResult(GameResult.PLAYER_WINS);
                player2.applyGameResult(GameResult.PLAYER_WINS);
            }

            // Then - Both should have same statistics regardless of name
            assertEquals(player1.getWins(), player2.getWins());
            assertEquals(5, player1.getWins());
            assertEquals(5, player2.getWins());
        }
    }

}
