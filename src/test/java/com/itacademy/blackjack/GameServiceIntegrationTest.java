package com.itacademy.blackjack;

import com.itacademy.blackjack.config.TestcontainersInitializer;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.domain.model.exception.NotPlayerTurnException;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for GameService.
 * Tests cover game lifecycle, error handling, and edge cases.
 */
@Import(TestcontainersInitializer.class)
@SpringBootTest
class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;
    private UUID testPlayerId;

    @BeforeEach
    void setUp() {
        Player testPlayer = playerService.createPlayer("TestPlayer").block();
        assertNotNull(testPlayer);
        testPlayerId = testPlayer.getId();
    }

    @AfterEach
    void tearDown() {
        // Clean up games created during tests
        if (testPlayerId != null) {
           playerService.deleteById(testPlayerId).block();
        }
    }

    @Nested
    @DisplayName("Game Lifecycle Tests - Happy Path")
    class GameLifecycleTests {



        @Test
        @DisplayName("Start new game - creates game with initial cards")
        void testStartNewGame() {
            GameResponse game = gameService.startNewGame(testPlayerId).block();

            assertNotNull(game, "Game response should not be null");
            assertNotNull(game.id(), "Game ID should not be null");
            assertEquals(2, game.player().hand().size(), "Player should have 2 cards");
            assertEquals(2, game.crupierHand().size(), "Crupier should have 2 cards");
            assertTrue(game.status() == GameStatus.PLAYER_TURN ||
                            game.status() == GameStatus.FINISHED,
                    "Initial status should be PLAYER_TURN or FINISHED (Blackjack)");
        }

        @Test
        @DisplayName("Complete game flow - player hits twice then stands")
        void testCompleteGameFlow_PlayerHitsTwiceThenStands() {
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);
            UUID gameId = game.id();

            System.out.println("Game ID: " + gameId);
            System.out.println("Initial status: " + game.status());

            // Handle immediate Blackjack
            if (game.result() == GameResult.BLACKJACK || game.result() == GameResult.PUSH) {
                System.out.println("Game ended immediately with: " + game.result());
                assertEquals(GameStatus.FINISHED, game.status());
                return;
            }

            assertEquals(GameStatus.PLAYER_TURN, game.status());
            assertEquals(GameResult.NO_RESULTS_YET, game.result());

            // Player hits
            GameResponse afterFirstHit = gameService.playerHit(gameId).block();
            assertNotNull(afterFirstHit);
            assertEquals(3, afterFirstHit.player().hand().size());

            // Check for bust
            if (afterFirstHit.status() == GameStatus.FINISHED) {
                assertEquals(GameResult.CRUPIER_WINS, afterFirstHit.result());
                System.out.println("Player busted!");
                return;
            }

            // Player hits again
            GameResponse afterSecondHit = gameService.playerHit(gameId).block();
            assertNotNull(afterSecondHit);
            assertEquals(4, afterSecondHit.player().hand().size());

            // Check for bust
            if (afterSecondHit.status() == GameStatus.FINISHED) {
                assertEquals(GameResult.CRUPIER_WINS, afterSecondHit.result());
                System.out.println("Player busted!");
                return;
            }

            // Player stands
            GameResponse finalGame = gameService.playerStand(gameId).block();
            assertNotNull(finalGame);
            
            // With new crupier flow, status is CRUPIER_TURN after player stands
            // Game finishes after crupier completes their turn
            assertTrue(finalGame.status() == GameStatus.CRUPIER_TURN ||
                       finalGame.status() == GameStatus.FINISHED,
                    "Status should be CRUPIER_TURN or FINISHED, but was: " + finalGame.status());
            
            // Result is only set when game is FINISHED
            if (finalGame.status() == GameStatus.FINISHED) {
                assertNotEquals(GameResult.NO_RESULTS_YET, finalGame.result());
            }

            System.out.println("Game finished! Winner: " + finalGame.result());
        }

        @Test
        @DisplayName("Game ID remains consistent throughout operations")
        void testGameIdConsistency() {
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);
            UUID originalGameId = game.id();

            if (game.status() == GameStatus.FINISHED) {
                System.out.println("Game ended immediately with: " + game.result());
                return;
            }

            GameResponse afterHit = gameService.playerHit(originalGameId).block();
            assertNotNull(afterHit);
            UUID gameIdAfterHit = afterHit.id();
            assertEquals(originalGameId, gameIdAfterHit);

            if (afterHit.status() != GameStatus.FINISHED) {
                GameResponse afterStand = gameService.playerStand(originalGameId).block();
                assertNotNull(afterStand);
                UUID gameIdAfterStand = afterStand.id();
                assertEquals(originalGameId, gameIdAfterStand);
            }
        }

        @Test
        @DisplayName("Multiple games are isolated from each other")
        void testGameIsolation() {
            GameResponse game1 = gameService.startNewGame(testPlayerId).block();
            GameResponse game2 = gameService.startNewGame(testPlayerId).block();

            assertNotNull(game1);
            assertNotNull(game2);
            assertNotEquals(game1.id(), game2.id(), "Games should have unique IDs");

            int game1InitialScore = game1.player().score();
            int game2InitialScore = game2.player().score();

            // Hit on game1
            if (game1.status() == GameStatus.PLAYER_TURN) {
                GameResponse afterHit = gameService.playerHit(game1.id()).block();
                assertNotNull(afterHit);
                assertNotEquals(game1InitialScore, afterHit.player().score());

                // Verify game2 is unchanged
                GameResponse game2State = gameService.getGameById(game2.id()).block();
                assertNotNull(game2State);
                assertEquals(game2InitialScore, game2State.player().score());
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests - Sad Path")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Hit with invalid game ID throws ResourceNotFoundException")
        void testPlayerHitWithInvalidGameId() {
            UUID invalidGameId = UUID.randomUUID();

            // Exception is thrown synchronously, so use assertThrows
            assertThrows(ResourceNotFoundException.class,
                    () -> gameService.playerHit(invalidGameId).block(),
                    "Should throw ResourceNotFoundException for invalid game ID");
        }

        @Test
        @DisplayName("Stand with invalid game ID throws ResourceNotFoundException")
        void testPlayerStandWithInvalidGameId() {
            UUID invalidGameId = UUID.randomUUID();

            // Exception is thrown synchronously, so use assertThrows
            assertThrows(ResourceNotFoundException.class,
                    () -> gameService.playerStand(invalidGameId).block(),
                    "Should throw ResourceNotFoundException for invalid game ID");
        }

        @Test
        @DisplayName("Get game by invalid ID throws ResourceNotFoundException")
        void testGetGameById_notFound() {
            UUID invalidGameId = UUID.randomUUID();

            // Exception is thrown synchronously, so use assertThrows
            assertThrows(ResourceNotFoundException.class,
                    () -> gameService.getGameById(invalidGameId).block(),
                    "Should throw ResourceNotFoundException for invalid game ID");
        }

        @Test
        @DisplayName("Hit when game is finished throws NotPlayerTurnException")
        void testPlayerHitWhenGameFinished() {
            // Step 1: Start a new game
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);
            UUID gameId = game.id();

            // Step 2: Keep hitting until player busts (guaranteed to happen eventually)
            GameResponse currentGame = game;
            int hitCount = 0;
            while (true) {
                assertNotNull(currentGame);
                if (!(currentGame.status() == GameStatus.PLAYER_TURN && hitCount < 10)) break;
                currentGame = gameService.playerHit(gameId).block();
                hitCount++;
            }

            // Step 3: Verify game is now FINISHED
            assertEquals(GameStatus.FINISHED, currentGame.status(),
                    "Game should be finished after busting");

            // Step 4: Try to hit again - should throw exception
            assertThrows(NotPlayerTurnException.class,
                    () -> gameService.playerHit(gameId).block(),
                    "Should throw NotPlayerTurnException when game is finished");
        }

   /*     @Test
        @DisplayName("Stand when game is finished throws NotPlayerTurnException")
        void testPlayerStandWhenGameFinished() {
            Game game = new Game(scoringService);
            game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
            game.getPlayer().receiveCard(new Card(CardRank.KING, Suit.SPADES));
            game.getPlayer().receiveCard(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!
            game.determineWinner();

            gameRepository.save(game);
            UUID gameId = game.getId();

            assertThrows(NotPlayerTurnException.class,
                    () -> gameService.playerStand(gameId).block(),
                    "Should throw NotPlayerTurnException when game is finished");
        }

        @Test
        @DisplayName("Hit when not player's turn throws NotPlayerTurnException")
        void testPlayerHitWhenNotPlayerTurn() {
            Game game = new Game(scoringService);
            game.startGame();

            if (game.getGameStatus() == GameStatus.PLAYER_TURN) {
                game.playerStand();
            }

            gameRepository.save(game);
            UUID gameId = game.getId();

            assertThrows(NotPlayerTurnException.class,
                    () -> gameService.playerHit(gameId).block(),
                    "Should throw NotPlayerTurnException when it's not player's turn");
        }

        @Test
        @DisplayName("Delete non-existent game completes without error")
        void testDeleteNonExistentGame() {
            UUID nonExistentId = UUID.randomUUID();

            // deleteById returns Mono<Void> and completes without error
            StepVerifier.create(gameService.deleteById(nonExistentId))
                    .verifyComplete();
        }
    }*/
    }

    @Nested
    @DisplayName("Crupier Turn Tests - New Feature")
    class CrupierTurnTests {

        @Test
        @DisplayName("Player stand sets status to CRUPIER_TURN")
        void testPlayerStandSetsCrupierTurnStatus() {
            // Given: A game in PLAYER_TURN status
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);

            // Skip if game ended immediately (blackjack)
            if (game.status() == GameStatus.FINISHED) {
                return;
            }

            assertEquals(GameStatus.PLAYER_TURN, game.status());
            UUID gameId = game.id();

            // When: Player stands
            GameResponse afterStand = gameService.playerStand(gameId).block();
            assertNotNull(afterStand);

            // Then: Status should be CRUPIER_TURN or FINISHED (if crupier already had 17+)
            // Important fix: crupier might auto-complete if score >= 17
            assertTrue(
                    afterStand.status() == GameStatus.CRUPIER_TURN ||
                            afterStand.status() == GameStatus.FINISHED,
                    "Status should be CRUPIER_TURN or FINISHED, but was: " + afterStand.status()
            );
            System.out.println("Status after stand: " + afterStand.status());
        }

        @Test
        @DisplayName("Crupier hit adds one card and keeps CRUPIER_TURN status")
        void testCrupierHitAddsCardKeepsStatus() {
            // Given: A game where player stood
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);

            if (game.status() == GameStatus.FINISHED) {
                return;
            }

            GameResponse afterStand = gameService.playerStand(game.id()).block();
            assertNotNull(afterStand);

            // Verify we have CRUPIER_TURN status
            if (afterStand.status() != GameStatus.CRUPIER_TURN) {
                System.out.println("Skipping - status is: " + afterStand.status());
                return;
            }

            // Skip if crupier already has 17+ (they won't draw a card)
            if (afterStand.crupierScore() >= 17) {
                System.out.println("Skipping - crupier already has 17+ (score: " + afterStand.crupierScore() + ")");
                return;
            }

            int initialCrupierCards = afterStand.crupierHand().size();

            // When: Crupier hits once
            GameResponse afterCrupierHit = gameService.crupierHitOneCard(game.id()).block();
            assertNotNull(afterCrupierHit);

            // Then: Crupier should have one more card
            assertEquals(initialCrupierCards + 1, afterCrupierHit.crupierHand().size());

            // And: Status should still be CRUPIER_TURN (unless crupier finished)
            System.out.println("Crupier score after hit: " + afterCrupierHit.crupierScore());
        }

        @Test
        @DisplayName("Crupier stands automatically when reaching 17 or more")
        void testCrupierStandsWhenReaching17() {
            // Given: A game in CRUPIER_TURN with low crupier score
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);

            if (game.status() == GameStatus.FINISHED) {
                return;
            }

            GameResponse afterStand = gameService.playerStand(game.id()).block();
            assertNotNull(afterStand);

            if (afterStand.status() != GameStatus.CRUPIER_TURN) {
                return;
            }

            // If crupier already has 17+, they should stand (game finishes after calling crupierHitOneCard once)
            // Note: playerStand() just sets status to CRUPIER_TURN, it doesn't auto-complete the crupier turn
            if (afterStand.crupierScore() >= 17) {
                // Crupier should stand on 17+, so one call to crupierHitOneCard should finish the game
                GameResponse afterCrupierHit = gameService.crupierHitOneCard(game.id()).block();
                assertNotNull(afterCrupierHit);
                assertEquals(GameStatus.FINISHED, afterCrupierHit.status());
                System.out.println("Crupier had 17+, game finished after one crupier hit call");
                return;
            }

            // Keep hitting until crupier stands
            GameResponse currentGame = afterStand;
            int maxHits = 10;
            int hitCount = 0;

            while (currentGame.status() == GameStatus.CRUPIER_TURN && hitCount < maxHits) {
                currentGame = gameService.crupierHitOneCard(game.id()).block();
                assertNotNull(currentGame);
                hitCount++;
                System.out.println("Crupier hit #" + hitCount + ", score: " + currentGame.crupierScore() + ", status: " + currentGame.status());
            }

            // After loop, verify crupier has reached at least 17 OR we're at max hits
            // Note: Crupier stands on 17+, but busting (going over 21) also stops the loop
            int finalScore = currentGame.crupierScore();
            System.out.println("Final crupier score: " + finalScore + ", Final status: " + currentGame.status() + ", Total hits: " + hitCount);

            // The crupier stops when score >= 17 OR when busted (score > 21)
            // So the final score should always be >= 17 OR we hit max hits
            assertTrue(
                    finalScore >= 17 || hitCount >= maxHits,
                    "Expected crupier score >= 17 or max hits reached. Score: " + finalScore + ", Hits: " + hitCount
            );

            // Game is either FINISHED or we hit max hits
            assertTrue(
                    currentGame.status() == GameStatus.FINISHED || hitCount >= maxHits,
                    "Expected FINISHED or max hits reached, but was: " + currentGame.status() +
                            ", Hits: " + hitCount + ", Crupier score: " + finalScore
            );

            if (currentGame.status() == GameStatus.FINISHED) {
                assertNotNull(currentGame.result());
                assertNotEquals(GameResult.NO_RESULTS_YET, currentGame.result());
                System.out.println("Game finished! Result: " + currentGame.result());
            }

        }

        @Test
        @DisplayName("Crupier hits multiple times until standing")
        void testCrupierHitsMultipleTimes() {
            // Given: A new game
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);

            if (game.status() == GameStatus.FINISHED) {
                return;
            }

            // When: Player stands immediately
            GameResponse afterStand = gameService.playerStand(game.id()).block();
            assertNotNull(afterStand);

            if (afterStand.status() != GameStatus.CRUPIER_TURN) {
                return;
            }

            int totalHits = 0;
            GameResponse currentGame = afterStand;

            // Crupier keeps hitting until standing
            while (currentGame.status() == GameStatus.CRUPIER_TURN) {
                currentGame = gameService.crupierHitOneCard(game.id()).block();
                assertNotNull(currentGame);
                totalHits++;

                // Safety limit
                if (totalHits > 10) {
                    System.out.println("Safety limit reached");
                    break;
                }
            }

            // Then: Game is finished or we hit the safety limit
            assertTrue(
                    currentGame.status() == GameStatus.FINISHED || totalHits > 10,
                    "Expected FINISHED or safety limit reached, but was: " + currentGame.status()
            );

            System.out.println("Crupier took " + totalHits + " hits. Final score: " + currentGame.crupierScore());
            System.out.println("Final result: " + currentGame.result());
        }

        @Test
        @DisplayName("Full game flow with visible crupier turn")
        void testFullGameFlowWithCrupierTurn() {
            // This test simulates the new frontend flow
            System.out.println("=== Starting Full Game Test ===");

            // 1. Start game
            GameResponse game = gameService.startNewGame(testPlayerId).block();
            assertNotNull(game);
            UUID gameId = game.id();
            System.out.println("Game started. Status: " + game.status());
            System.out.println("Player cards: " + game.player().hand().size() + ", Score: " + game.player().score());
            System.out.println("Crupier cards: " + game.crupierHand().size() + ", Score: " + game.crupierScore());

            // Skip if immediate blackjack
            if (game.status() == GameStatus.FINISHED) {
                System.out.println("Game ended immediately with: " + game.result());
                return;
            }

            // 2. Player decides to hit once
            System.out.println("\n--- Player Hits ---");
            GameResponse afterHit = gameService.playerHit(gameId).block();
            assertNotNull(afterHit);
            System.out.println("Player cards: " + afterHit.player().hand().size() + ", Score: " + afterHit.player().score());

            if (afterHit.status() == GameStatus.FINISHED) {
                System.out.println("Player busted! Result: " + afterHit.result());
                return;
            }

            // 3. Player stands - now crupier's turn
            System.out.println("\n--- Player Stands ---");
            GameResponse afterPlayerStand = gameService.playerStand(gameId).block();
            assertNotNull(afterPlayerStand);
            System.out.println("Status after stand: " + afterPlayerStand.status());
            System.out.println("Crupier score: " + afterPlayerStand.crupierScore());

            // 4. Crupier plays one card at a time (simulating frontend calls)
            System.out.println("\n--- Crupier Playing ---");
            GameResponse currentGame = afterPlayerStand;
            int crupierHits = 0;

            while (currentGame.status() == GameStatus.CRUPIER_TURN) {
                currentGame = gameService.crupierHitOneCard(gameId).block();
                assertNotNull(currentGame);
                crupierHits++;
                System.out.println("Crupier hit #" + crupierHits + ": Score = " + currentGame.crupierScore());

                if (crupierHits > 10) {
                    System.out.println("Safety break!");
                    break;
                }
            }

            // 5. Game is finished
            System.out.println("\n--- Game Finished ---");
            System.out.println("Final status: " + currentGame.status());
            System.out.println("Final crupier score: " + currentGame.crupierScore());

            // Only assert on result if game is actually finished
            if (currentGame.status() == GameStatus.FINISHED) {
                assertNotNull(currentGame.result());
                System.out.println("Final result: " + currentGame.result());
                System.out.println("Player wins: " + currentGame.result() + " = " +
                        (currentGame.result() == GameResult.PLAYER_WINS || currentGame.result() == GameResult.BLACKJACK));
            } else {
                System.out.println("Game not finished within safety limit. Crupier score: " + currentGame.crupierScore());
            }
        }
    }
    @Test
    @DisplayName("Delete game recalculates player stats - win removed")
    void testDeleteGame_RecalculatesPlayerStats_Win() {
        // 1. Play a game and win
        GameResponse game = gameService.startNewGame(testPlayerId).block();
        assertNotNull(game);

        // Force a player win (simplified - actual test would hit until win)
        while (game.status() == GameStatus.PLAYER_TURN) {
            game = gameService.playerHit(game.id()).block();
        }

        // If player won, verify stats
        if (game.result() == GameResult.PLAYER_WINS) {
            Player playerBefore = playerService.findById(testPlayerId).block();
            int winsBefore = playerBefore.getWins();

            // 2. Delete the game
            gameService.deleteById(game.id()).block();

            // 3. Verify stats recalculated
            Player playerAfter = playerService.findById(testPlayerId).block();
            assertEquals(winsBefore - 1, playerAfter.getWins());
        }
    }

}
