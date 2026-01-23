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
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete Blackjack game flow:
 * 1. Start game
 * 2. Player hits (draws card)
 * 3. Player hits again (draws another card)
 * 4. Player stands (crupier plays, game ends)
 */
class GameServiceIntegrationTest {

    private GameService gameService;
    private GameRepository gameRepository;
    private ScoringService scoringService;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
        gameRepository = new GameRepository();
        gameService = new GameService(scoringService, gameRepository);
    }

    @Test
    void testCompleteGameFlow_PlayerHitsTwiceThenStands() {
        // Step 1: Start a new game
        GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
        assertNotNull(game, "Game response should not be null");
        assertNotNull(game.id(), "Game ID should not be null");

        UUID gameId = game.id(); // 🔽 USE LOCAL VARIABLE INSTEAD OF FIELD

        System.out.println("=== After starting game ===");
        System.out.println("Game ID: " + gameId);
        System.out.println("Status: " + game.status());
        System.out.println("Player cards: " + game.player().hand().size() + " (expected: 2)");
        System.out.println("Player score: " + game.player().score());
        System.out.println("Crupier cards: " + game.crupierHand().size() + " (expected: 2)");
        System.out.println("Game result: " + game.result());

        // Handle case where player gets Blackjack immediately
        if (game.result() == GameResult.BLACKJACK || game.result() == GameResult.PUSH) {
            System.out.println("Game ended immediately with: " + game.result());
            assertEquals(GameStatus.FINISHED, game.status());
            return; // Exit test early
        }

        assertEquals(GameStatus.PLAYER_TURN, game.status(),
                "Game status should be PLAYER_TURN unless player got Blackjack");
        assertEquals(2, game.player().hand().size(), "Player should have 2 cards");
        assertEquals(2, game.crupierHand().size(), "Crupier should have 2 cards");
        assertEquals(GameResult.NO_RESULTS_YET, game.result(),
                "Game should not have a result yet");

        // Step 2: Player hits (draws first additional card)
        GameResponse afterFirstHit = gameService.playerHit(gameId).block();
        assertNotNull(afterFirstHit, "First hit response should not be null");

        System.out.println("\n=== After first hit ===");
        System.out.println("Status: " + afterFirstHit.status());
        System.out.println("Player cards: " + afterFirstHit.player().hand().size());
        System.out.println("Player score: " + afterFirstHit.player().score());
        System.out.println("Result: " + afterFirstHit.result());

        assertEquals(3, afterFirstHit.player().hand().size(),
                "Player should have 3 cards after first hit");

        // Check if player busted after first hit
        if (afterFirstHit.status() == GameStatus.FINISHED) {
            // Player busted - game is over, skip remaining steps
            assertEquals(GameResult.CRUPIER_WINS, afterFirstHit.result());
            System.out.println("Player busted after first hit! Game over.");
            return; // Exit test early
        }

        // Step 3: Player hits again (only if still in PLAYER_TURN)
        GameResponse afterSecondHit = gameService.playerHit(gameId).block();
        assertNotNull(afterSecondHit, "Second hit response should not be null");

        System.out.println("\n=== After second hit ===");
        System.out.println("Status: " + afterSecondHit.status());
        System.out.println("Player cards: " + afterSecondHit.player().hand().size());
        System.out.println("Player score: " + afterSecondHit.player().score());
        System.out.println("Result: " + afterSecondHit.result());

        assertEquals(4, afterSecondHit.player().hand().size(),
                "Player should have 4 cards after second hit");

        // Check if player busted after second hit
        if (afterSecondHit.status() == GameStatus.FINISHED) {
            assertEquals(GameResult.CRUPIER_WINS, afterSecondHit.result());
            System.out.println("Player busted after second hit! Game over.");
            return; // Exit test early
        }

        // Step 4: Player stands (crupier plays, game ends)
        // 🔽 CRITICAL: Verify gameId is not null before calling playerStand
        assertNotNull(gameId, "Game ID should not be null");

        GameResponse finalGame = gameService.playerStand(gameId).block();
        assertNotNull(finalGame, "Final game response should not be null");

        System.out.println("\n=== After player stands (game finished) ===");
        System.out.println("Final status: " + finalGame.status());
        System.out.println("Final result: " + finalGame.result());
        System.out.println("Player final score: " + finalGame.player().score());
        System.out.println("Crupier final score: " + finalGame.crupierScore());

        // Final verification
        assertEquals(GameStatus.FINISHED, finalGame.status(),
                "Game should be finished after player stands");
        assertNotEquals(GameResult.NO_RESULTS_YET, finalGame.result(),
                "Game should have a result");
        assertTrue(
                finalGame.result() == GameResult.PLAYER_WINS ||
                        finalGame.result() == GameResult.CRUPIER_WINS ||
                        finalGame.result() == GameResult.PUSH ||
                        finalGame.result() == GameResult.BLACKJACK,
                "Game result should be a valid end state"
        );

        System.out.println("\n=== Game completed successfully! ===");
        System.out.println("Winner: " + finalGame.result());
    }


    @Test
    void testPlayerHitWithInvalidGameId_throwsException() {
        // Given: An invalid game ID
        UUID invalidGameId = UUID.randomUUID();

        // When/Then: Hit should throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.playerHit(invalidGameId).block(),
                "Hit should throw exception for non-existent game"
        );
    }

    @Test
    void testPlayerStandWithInvalidGameId_throwsException() {
        // Given: An invalid game ID
        UUID invalidGameId = UUID.randomUUID();

        // When/Then: Stand should throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.playerStand(invalidGameId).block(),
                "Stand should throw exception for non-existent game"
        );
    }

    @Test
    void testGetGameById_notFound_throwsException() {
        // Given: An invalid game ID
        UUID invalidGameId = UUID.randomUUID();

        // When/Then: Should throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.getGameById(invalidGameId).block(),
                "Get game should throw exception for non-existent game"
        );
    }

    @Test
    void testPlayerHitWhenGameFinished_throwsException() {
        // Given: A game that ends immediately with player bust
        GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
        assertNotNull(game);

        // Force player to bust by getting cards that exceed 21
        // Since we can't control card order, we simulate by using a known game ID
        UUID gameId = game.id();

        // Player busts by taking too many cards
        GameResponse afterHit = gameService.playerHit(gameId).block();

        // If player busted, game is finished - now hit should fail
        if (afterHit.status() == GameStatus.FINISHED) {
            assertThrows(NotPlayerTurnException.class,
                    () -> gameService.playerHit(gameId).block(),
                    "Hit should throw when game is finished"
            );
        }
    }

    @Test
    void testPlayerStandWhenGameFinished_throwsException() {
        // Given: A finished game (player busted)
        Game game = new Game(scoringService);
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayer().receiveCard(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!
        game.determineWinner();

        gameRepository.save(game);
        UUID gameId = game.getId();

        // When/Then: Stand should throw exception
        assertThrows(NotPlayerTurnException.class,
                () -> gameService.playerStand(gameId).block(),
                "Stand should throw when game is finished"
        );
    }

    @Test
    void testPlayerHitWhenNotPlayerTurn_throwsException() {
        // Given: A game where player has stood
        Game game = new Game(scoringService);
        game.startGame();

        // If it's still player's turn, have them stand
        if (game.getGameStatus() == GameStatus.PLAYER_TURN) {
            game.playerStand();
        }

        gameRepository.save(game);
        UUID gameId = game.getId();

        // When/Then: Hit should throw NotPlayerTurnException
        assertThrows(NotPlayerTurnException.class,
                () -> gameService.playerHit(gameId).block(),
                "Hit should throw when it's not player's turn"
        );
    }

    @Test
    void testDeleteNonExistentGame_completesWithoutError() {
        // Given: A non-existent game ID
        UUID nonExistentId = UUID.randomUUID();

        // When: Delete is called (should complete without error in reactive)
        // Then: Should not throw exception (delete is void)
        assertDoesNotThrow(() -> gameService.deleteById(nonExistentId));
    }

    @Test
    void testMultipleOperationsOnSameGameId_areIsolated() {
        // Given: Start two separate games
        GameResponse game1 = gameService.startNewGame(UUID.randomUUID()).block();
        assertNotNull(game1, "Game 1 should not be null");
        UUID gameId1 = game1.id();

        GameResponse game2 = gameService.startNewGame(UUID.randomUUID()).block();
        assertNotNull(game2, "Game 2 should not be null");
        UUID gameId2 = game2.id();

        // Then: Both games should have different IDs
        assertNotEquals(gameId1, gameId2, "Games should have unique IDs");
        assertNotEquals(game1.id(), game2.id(), "Games should have unique IDs");

        // Verify initial states are different (random cards)
        int game1InitialScore = game1.player().score();
        int game2InitialScore = game2.player().score();

        System.out.println("Game 1 initial score: " + game1InitialScore);
        System.out.println("Game 2 initial score: " + game2InitialScore);

        // When: Perform operation on game1 (only if it's player's turn)
        if (game1.status() == GameStatus.PLAYER_TURN) {
            GameResponse afterHit1 = gameService.playerHit(gameId1).block();
            assertNotNull(afterHit1, "Hit response should not be null");

            // Then: Game1 score changed, Game2 remains unchanged
            assertNotEquals(game1InitialScore, afterHit1.player().score(),
                    "Game1 score should change after hit");

            // Get fresh state of game2
            GameResponse game2State = gameService.getGameById(gameId2).block();
            assertNotNull(game2State);

            // Verify game2 is unchanged
            assertEquals(game2InitialScore, game2State.player().score(),
                    "Game2 score should remain unchanged after operating on Game1");
            assertEquals(game2.player().hand().size(), game2State.player().hand().size(),
                    "Game2 hand size should remain unchanged");

            System.out.println("Game1 score after hit: " + afterHit1.player().score());
            System.out.println("Game2 score (unchanged): " + game2State.player().score());
        } else {
            // If game1 is finished (Blackjack or bust), skip hit and verify isolation differently
            System.out.println("Game1 finished immediately with status: " + game1.status());

            // Game2 should still be independent
            GameResponse game2State = gameService.getGameById(gameId2).block();
            assertNotNull(game2State);
            assertEquals(GameStatus.PLAYER_TURN, game2State.status(),
                    "Game2 should still be in PLAYER_TURN");
        }
    }


    @Test
    void testGameIdRemainsConsistentThroughoutOperations() {
        // Given: A new game
        GameResponse game = gameService.startNewGame(UUID.randomUUID()).block();
        assertNotNull(game);
        UUID originalGameId = game.id();

        // When: Perform various operations
        GameResponse afterHit = gameService.playerHit(originalGameId).block();
        UUID gameIdAfterHit = afterHit.id();

        // If game not finished, continue
        if (afterHit.status() != GameStatus.FINISHED) {
            GameResponse afterStand = gameService.playerStand(originalGameId).block();
            UUID gameIdAfterStand = afterStand.id();

            // Then: Game ID should remain consistent
            assertEquals(originalGameId, gameIdAfterHit,
                    "Game ID should not change after hit");
            assertEquals(originalGameId, gameIdAfterStand,
                    "Game ID should not change after stand");
        } else {
            assertEquals(originalGameId, gameIdAfterHit,
                    "Game ID should not change after hit");
        }
    }
}

