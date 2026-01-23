package com.itacademy.blackjack;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
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
        assertNotNull(game);
        gameId = game.id();

        System.out.println("=== After starting game ===");
        System.out.println("Game ID: " + gameId);
        System.out.println("Status: " + game.status());
        System.out.println("Player cards: " + game.player().hand().size() + " (expected: 2)");
        System.out.println("Player score: " + game.player().score());
        System.out.println("Crupier cards: " + game.crupierHand().size() + " (expected: 2)");

        assertEquals(GameStatus.PLAYER_TURN, game.status());
        assertEquals(2, game.player().hand().size());
        assertEquals(2, game.crupierHand().size());
        assertEquals(GameResult.NO_RESULTS_YET, game.result());

        // Step 2: Player hits (draws first additional card)
        GameResponse afterFirstHit = gameService.playerHit(gameId).block();
        assertNotNull(afterFirstHit);

        assertEquals(3, afterFirstHit.player().hand().size());

        // Check if player busted after first hit
        if (afterFirstHit.status() == GameStatus.FINISHED) {
            // Player busted - game is over, skip remaining steps
            assertEquals(GameResult.CRUPIER_WINS, afterFirstHit.result());
            System.out.println("Player busted after first hit! Game over.");
            return;  // Exit test early
        }

        // Step 3: Player hits again (only if still in PLAYER_TURN)
        GameResponse afterSecondHit = gameService.playerHit(gameId).block();
        assertNotNull(afterSecondHit);

        System.out.println("\n=== After second player hit ===");
        System.out.println("Status: " + afterSecondHit.status());
        System.out.println("Player cards: " + afterSecondHit.player().hand().size() + " (expected: 4)");
        System.out.println("Player score: " + afterSecondHit.player().score());

        assertEquals(4, afterSecondHit.player().hand().size());

        // Step 4: Player stands (crupier plays, game ends)
        GameResponse finalGame = gameService.playerStand(gameId).block();
        assertNotNull(finalGame);

        System.out.println("\n=== After player stands (game finished) ===");
        System.out.println("Final status: " + finalGame.status());
        System.out.println("Final result: " + finalGame.result());
        System.out.println("Player final score: " + finalGame.player().score());
        System.out.println("Crupier final score: " + finalGame.crupierScore());

        // Final verification
        assertEquals(GameStatus.FINISHED, finalGame.status());
        assertNotEquals(GameResult.NO_RESULTS_YET, finalGame.result());
        assertTrue(
                finalGame.result() == GameResult.PLAYER_WINS ||
                        finalGame.result() == GameResult.CRUPIER_WINS ||
                        finalGame.result() == GameResult.PUSH
        );

        System.out.println("\n=== Game completed successfully! ===");
    }
}
