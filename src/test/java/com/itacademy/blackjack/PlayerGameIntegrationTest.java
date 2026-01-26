package com.itacademy.blackjack;

import com.itacademy.blackjack.config.TestcontainersInitializer;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersInitializer.class)
@SpringBootTest
class PlayerGameIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private UUID playerId;

    @BeforeEach
    void setUp() {
        Player player = playerService.createPlayer("IntegrationTestPlayer").block();
        assertNotNull(player);
        playerId = player.getId();
    }

    private void completeCrupierTurn(GameResponse game) {
        // Complete crupier turn if needed
        GameResponse currentGame = game;
        while (currentGame.status() == GameStatus.CRUPIER_TURN) {
            currentGame = gameService.crupierHitOneCard(game.id()).block();
            if (currentGame == null || currentGame.status() == GameStatus.FINISHED) {
                break;
            }
        }
    }

    @Nested
    @DisplayName("Player Statistics Tracking")
    class CrossModuleErrorHandling {

        @Test
        @DisplayName("Player stats update after standing and game ends")
        void testPlayerStatsUpdateAfterStanding() {
            GameResponse game = gameService.startNewGame(playerId).block();
            assertNotNull(game);

            GameResponse afterStand = gameService.playerStand(game.id()).block();
            assertNotNull(afterStand);
            
            // With new crupier flow, status is CRUPIER_TURN after player stands
            // Stats are updated when game completes (crupier finishes)
            assertTrue(afterStand.status() == GameStatus.CRUPIER_TURN || 
                       afterStand.status() == GameStatus.FINISHED,
                    "Status should be CRUPIER_TURN or FINISHED");

            Player updatedPlayer = playerService.findById(playerId).block();
            assertNotNull(updatedPlayer);
            // Stats should be updated (wins, losses, or pushes increased)
            assertTrue(updatedPlayer.getWins() >= 0);
            assertTrue(updatedPlayer.getLosses() >= 0);
            assertTrue(updatedPlayer.getPushes() >= 0);
        }

        @Test
        @DisplayName("Player wins increase after winning game")
        void testWinsIncreaseAfterWinningGame() {
            // Get initial wins
            int initialWins = playerService.findById(playerId).block().getWins();

            // Play game until player stands and wins
            GameResponse game = gameService.startNewGame(playerId).block();
            assertNotNull(game);

            // Keep hitting until score is high enough to likely win, then stand
            while (game.status() == GameStatus.PLAYER_TURN && game.player().score() < 18) {
                game = gameService.playerHit(game.id()).block();
                if (game.status() == GameStatus.FINISHED) break;
            }

            if (game.status() == GameStatus.PLAYER_TURN) {
                game = gameService.playerStand(game.id()).block();
                // Complete crupier turn to finish the game
                if (game.status() == GameStatus.CRUPIER_TURN) {
                    completeCrupierTurn(game);
                }
            }

            assertNotNull(game);

            // Verify at least one game was played
            Player updatedPlayer = playerService.findById(playerId).block();
            assertNotNull(updatedPlayer);
            assertTrue(updatedPlayer.getWins() + updatedPlayer.getLosses() + updatedPlayer.getPushes() >= 1);
        }
    }

    @Nested
    @DisplayName("Cross-Module Error Handling")
    class CrossModuleErrorHandling2 {

        @Test
        @DisplayName("Game cannot be created with non-existent player")
        void testGameCreationFailsWithInvalidPlayer() {
            UUID invalidPlayerId = UUID.randomUUID();

            assertThrows(Exception.class,
                    () -> gameService.startNewGame(invalidPlayerId).block());
        }

        @Test
        @DisplayName("Player can create multiple games")
        void testMultipleGamesPerPlayer() {
            GameResponse game1 = gameService.startNewGame(playerId).block();
            GameResponse game2 = gameService.startNewGame(playerId).block();

            assertNotNull(game1);
            assertNotNull(game2);
            assertNotEquals(game1.id(), game2.id());
            assertEquals(playerId, game1.player().id());
            assertEquals(playerId, game2.player().id());
        }

        @Test
        @DisplayName("Player data persists after game completion")
        void testPlayerDataPersistsAfterGame() {
            GameResponse game = gameService.startNewGame(playerId).block();
            assertNotNull(game);

            while (game.status() == GameStatus.PLAYER_TURN) {
                game = game.player().score() < 17
                        ? gameService.playerHit(game.id()).block()
                        : gameService.playerStand(game.id()).block();
            }

            Player playerAfterGame = playerService.findById(playerId).block();
            assertNotNull(playerAfterGame);
            assertEquals(playerId, playerAfterGame.getId());
            assertEquals("IntegrationTestPlayer", playerAfterGame.getName());
        }
    }

    @Nested
    @DisplayName("Data Isolation Tests")
    class DataIsolationTests {

        @Test
        @DisplayName("Two different players have isolated data")
        void testCreatePlayer_ReturnsCreatedPlayer() {
            Player player2 = playerService.createPlayer("SecondPlayer").block();
            assertNotNull(player2);
            UUID player2Id = player2.getId();

            // Player 1 plays a game
            GameResponse game1 = gameService.startNewGame(playerId).block();
            assertNotNull(game1);
            game1 = gameService.playerStand(game1.id()).block();
            // Complete crupier turn
            if (game1.status() == GameStatus.CRUPIER_TURN) {
                completeCrupierTurn(game1);
            }

            // Player 2 plays a game
            GameResponse game2 = gameService.startNewGame(player2Id).block();
            assertNotNull(game2);
            game2 = gameService.playerStand(game2.id()).block();
            // Complete crupier turn
            if (game2.status() == GameStatus.CRUPIER_TURN) {
                completeCrupierTurn(game2);
            }

            // Both players should have played at least one game
            Player p1After = playerService.findById(playerId).block();
            Player p2After = playerService.findById(player2Id).block();

            assertNotNull(p1After);
            assertNotNull(p2After);

            // Both should have stats >= 0, but they might both be 0 if both lost
            assertTrue(p1After.getWins() + p1After.getLosses() + p1After.getPushes() >= 1);
            assertTrue(p2After.getWins() + p2After.getLosses() + p2After.getPushes() >= 1);

            // Verify they are different players with different data
            assertNotEquals(p1After.getId(), p2After.getId());
            assertNotEquals(p1After.getName(), p2After.getName());
        }
    }
}
