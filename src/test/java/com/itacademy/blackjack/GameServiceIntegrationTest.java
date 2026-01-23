package com.itacademy.blackjack;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.domain.model.exception.NotPlayerTurnException;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import com.itacademy.blackjack.game.domain.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for GameService.
 * Tests cover game lifecycle, error handling, and edge cases.
 */
class GameServiceIntegrationTest {

    private GameService gameService;
    private GameRepository gameRepository;
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
        gameRepository = new GameRepository();
        gameService = new GameService(scoringService, gameRepository);
    }

    @Nested
    @DisplayName("Game Lifecycle Tests - Happy Path")
    class GameLifecycleTests {

        @Test
        @DisplayName("Start new game - creates game with initial cards")
        void testStartNewGame() {
            GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();

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
            GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
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
            assertEquals(GameStatus.FINISHED, finalGame.status());
            assertNotEquals(GameResult.NO_RESULTS_YET, finalGame.result());

            System.out.println("Game finished! Winner: " + finalGame.result());
        }

        @Test
        @DisplayName("Game ID remains consistent throughout operations")
        void testGameIdConsistency() {
            GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
            assertNotNull(game);
            UUID originalGameId = game.id();

            GameResponse afterHit = gameService.playerHit(originalGameId).block();
            UUID gameIdAfterHit = afterHit.id();
            assertEquals(originalGameId, gameIdAfterHit);

            if (afterHit.status() != GameStatus.FINISHED) {
                GameResponse afterStand = gameService.playerStand(originalGameId).block();
                UUID gameIdAfterStand = afterStand.id();
                assertEquals(originalGameId, gameIdAfterStand);
            }
        }

        @Test
        @DisplayName("Multiple games are isolated from each other")
        void testGameIsolation() {
            GameResponse game1 = gameService.startNewGame(UUID.randomUUID()).block();
            GameResponse game2 = gameService.startNewGame(UUID.randomUUID()).block();

            assertNotNull(game1);
            assertNotNull(game2);
            assertNotEquals(game1.id(), game2.id(), "Games should have unique IDs");

            int game1InitialScore = game1.player().score();
            int game2InitialScore = game2.player().score();

            // Hit on game1
            if (game1.status() == GameStatus.PLAYER_TURN) {
                GameResponse afterHit = gameService.playerHit(game1.id()).block();
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
            GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
            assertNotNull(game);
            UUID gameId = game.id();

            // Step 2: Keep hitting until player busts (guaranteed to happen eventually)
            GameResponse currentGame = game;
            int hitCount = 0;
            while (currentGame.status() == GameStatus.PLAYER_TURN && hitCount < 10) {
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

        @Test
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
    }

}
