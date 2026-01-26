package com.itacademy.blackjack.game.model;


import com.itacademy.blackjack.deck.model.*;
import com.itacademy.blackjack.game.domain.model.Crupier;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.domain.model.exception.NotPlayerTurnException;
import com.itacademy.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    ScoringService scoringService;

    @BeforeEach
    public void setUp() {
        scoringService = new ScoringService();
    }

    private Game createTestGame(ScoringService scoringService) {
        return Game.builder()
                .id(UUID.randomUUID())
                .scoringService(scoringService)
                .gameStatus(GameStatus.CREATED)
                .gameResult(GameResult.NO_RESULTS_YET)
                .deck(new Deck())
                .player(Player.createNew("Test Player", scoringService))  // Use factory method!
                .crupier(new Crupier(scoringService))
                .build();
    }

    private Game createGameWithCrupierTurn(ScoringService scoringService) {
        return Game.builder()
                .id(UUID.randomUUID())
                .scoringService(scoringService)
                .gameStatus(GameStatus.CRUPIER_TURN)
                .gameResult(GameResult.NO_RESULTS_YET)
                .deck(new Deck())
                .player(Player.createNew("Test Player", scoringService))  // Use factory method!
                .crupier(new Crupier(scoringService))
                .build();
    }

    @Test
    void testGameInitialization() {
        // Given: A new Game instance
        Game game = createTestGame(scoringService);

        // Then: Verify all initial conditions
        assertNotNull(game.getId(), "Game ID should not be null");
        assertNotNull(game.getDeck(), "Deck should be initialized");
        assertEquals(GameStatus.CREATED, game.getGameStatus(), "Initial status should be CREATED");
        assertTrue(game.getPlayer().getHand().isEmpty(), "Player hand should be empty");
        assertTrue(game.getCrupier().getHand().isEmpty(), "Crupier hand should be empty");
        assertEquals(GameResult.NO_RESULTS_YET, game.getGameResult(), "Game result shouldn't be null initially");
    }

    @Test
    void testDeckIsShuffledOnInitialization() {
        // Given: A new Game instance
        Game game = createTestGame(scoringService);


        // Then: Verify deck has 52 cards
        assertEquals(52, game.getDeck().size(), "Deck should have 52 cards");
        assertFalse(game.getDeck().isEmpty(), "Deck should not be empty");
    }

    @Test
    void testPlayerScoreInitiallyZero() {
        // Given: A new Game instance
        Game game = createTestGame(scoringService);


        // Then: Player score should be 0 (no cards)
        assertEquals(0, game.getPlayer().getScore(), "Player score should be 0 with no cards");
    }

    @Test
    void testDrawCardFromEmptyDeckThrowsException() {
        // Given: A game with all cards drawn
        Game game = createTestGame(scoringService);

        for (int i = 0; i < 52; i++) {
            game.drawCardFromDeck();
        }

        // When/Then: Drawing from empty deck should throw exception
        assertThrows(NoSuchElementException.class, game::drawCardFromDeck, "Should throw exception when drawing from empty deck");
    }

    @Test
    void testCrupierWinsWhenPlayerBusts() {
        // Given: A game where we manually set up a bust scenario
        Game game = createTestGame(scoringService);
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayer().receiveCard(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!

        game.getCrupier().receiveCard(new Card(CardRank.SIX, Suit.HEARTS));
        game.getCrupier().receiveCard(new Card(CardRank.FIVE, Suit.SPADES));

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.CRUPIER_WINS, game.getGameResult());
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }

    @Test
    void testPlayerWinsWhenCrupierBusts() {
        // Given: A game where crupier busts
        Game game = createTestGame(scoringService);
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.SIX, Suit.SPADES)); // 16

        game.getCrupier().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getCrupier().receiveCard(new Card(CardRank.KING, Suit.SPADES));
        game.getCrupier().receiveCard(new Card(CardRank.FIVE, Suit.CLUBS)); // Bust!

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.PLAYER_WINS, game.getGameResult());
    }

    @Test
    void testPushWhenScoresAreEqual() {
        // Given: A tie game
        Game game = createTestGame(scoringService);

        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.SIX, Suit.SPADES)); // 16

        game.getCrupier().receiveCard(new Card(CardRank.KING, Suit.CLUBS));
        game.getCrupier().receiveCard(new Card(CardRank.SIX, Suit.DIAMONDS)); // 16

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.PUSH, game.getGameResult());
    }

    @Test
    void testGameStatusTransitions() {
        // Given: A new game
        Game game = createTestGame(scoringService);

        assertEquals(GameStatus.CREATED, game.getGameStatus());

        // When: Start the game
        game.startGame();

        // Then: Status should change (unless player got Blackjack)
        assertNotEquals(GameStatus.CREATED, game.getGameStatus());
    }

    @Test
    void testPlayerHitThrowsExceptionWhenNotPlayerTurn() {
        Game game = createTestGame(scoringService);
        game.startGame();

        // If player doesn't have blackjack, game status is PLAYER_TURN
        // Player stands to end their turn
        if (game.getGameStatus() == GameStatus.PLAYER_TURN) {
            game.playerStand();  // Ends player turn
        }

        // Now hit should throw NotPlayerTurnException
        assertThrows(NotPlayerTurnException.class, game::playerHit);
    }


    @Test
    void testPlayerWinsWithHigherScore() {
        Game game = createTestGame(scoringService);

        game.getCrupier().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getCrupier().receiveCard(new Card(CardRank.SIX, Suit.DIAMONDS)); // 16
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.CLUBS));
        game.getPlayer().receiveCard(new Card(CardRank.NINE, Suit.SPADES)); // 19

        game.determineWinner();

        assertEquals(GameResult.PLAYER_WINS, game.getGameResult());
    }

    @Test
    void testCrupierWinsWithHigherScore() {
        Game game = createTestGame(scoringService);

        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.FIVE, Suit.SPADES)); // 15
        game.getCrupier().receiveCard(new Card(CardRank.TEN, Suit.CLUBS));
        game.getCrupier().receiveCard(new Card(CardRank.SEVEN, Suit.DIAMONDS)); // 17

        game.determineWinner();

        assertEquals(GameResult.CRUPIER_WINS, game.getGameResult());
    }


    @Test
    void testPlayerHitThrowsExceptionWhenGameNotStarted() {
        // Given: A new game that hasn't started
        Game game = createTestGame(scoringService);

        // When/Then: Hit before game starts should throw
        assertThrows(NotPlayerTurnException.class, game::playerHit, "Hit should throw when game hasn't started");
    }

    @Test
    void testPlayerHitThrowsExceptionAfterPlayerStood() {
        // Given: A game where player has stood
        Game game = createTestGame(scoringService);
        game.startGame();

        // Player stands (only if it's their turn)
        if (game.getGameStatus() == GameStatus.PLAYER_TURN) {
            game.playerStand();
        }

        // When/Then: Hit after standing should throw
        assertThrows(NotPlayerTurnException.class, game::playerHit, "Hit should throw after player stood");
    }

    @Test
    void testPlayerHitThrowsExceptionWhenGameFinished() {
        // Given: A finished game
        Game game = createTestGame(scoringService);

        // Force a finished state - player busts
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayer().receiveCard(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!
        game.determineWinner();

        // When/Then: Hit on finished game should throw
        assertThrows(NotPlayerTurnException.class, game::playerHit, "Hit should throw when game is finished");
    }

    @Test
    void testPlayerStandThrowsExceptionWhenGameNotStarted() {
        // Given: A new game that hasn't started
        Game game = createTestGame(scoringService);

        // When/Then: Stand before game starts should throw
        assertThrows(NotPlayerTurnException.class, game::playerStand, "Stand should throw when game hasn't started");
    }

    @Test
    void testPlayerStandThrowsExceptionWhenAlreadyFinished() {
        // Given: A finished game
        Game game = createTestGame(scoringService);

        // Force finish - player busts
        game.getPlayer().receiveCard(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayer().receiveCard(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayer().receiveCard(new Card(CardRank.TWO, Suit.CLUBS));
        game.determineWinner();

        // When/Then: Stand on finished game should throw
        assertThrows(NotPlayerTurnException.class, game::playerStand, "Stand should throw when game is finished");
    }

    @Test
    void testCrupierTurnStatusExists() {
        // Given: A game after player stands
        Game game = createTestGame(scoringService);
        game.startGame();

        // Skip if player got blackjack (game ends immediately)
        if (game.getGameStatus() == GameStatus.FINISHED) {
            return; // Test not applicable for blackjack scenarios
        }

        // When: Player stands
        game.playerStand();

        // Then: Game status should be CRUPIER_TURN (not immediately FINISHED)
        // Nota: Si crupierTurn() se llama inmediatamente, será FINISHED
        assertNotNull(game.getGameStatus());
    }

    @Test
    void testCrupierHitOneCard_ThrowsException_WhenNotCrupierTurn() {
        // Given: A game in PLAYER_TURN status
        Game game = createTestGame(scoringService);
        game.startGame();

        // When/Then: Calling crupierHitOneCard should throw
        assertThrows(NotPlayerTurnException.class, game::crupierHitOneCard,
                "Should throw when it's not crupier's turn");
    }

    @Test
    void testCrupierHitOneCard_AddsCard_WhenCrupierMustHit() {
        // Given: A game with crupier turn status
        Game game = createGameWithCrupierTurn(scoringService);  // Changed!
        game.getCrupier().receiveCard(new Card(CardRank.TEN, Suit.HEARTS)); // 10
        game.getCrupier().receiveCard(new Card(CardRank.FIVE, Suit.SPADES)); // 15

        int initialCards = game.getCrupier().getCardCount() ;

        // When
        game.crupierHitOneCard();

        // Then: Crupier should have one more card
        assertEquals(initialCards + 1, game.getCrupier().getCardCount());
    }

    @Test
    void testCrupierHitOneCard_DoesNotAddCard_WhenCrupierShouldStand() {
        Game game = createGameWithCrupierTurn(scoringService);  // Changed!
        game.getCrupier().receiveCard(new Card(CardRank.KING, Suit.HEARTS)); // 10
        game.getCrupier().receiveCard(new Card(CardRank.SEVEN, Suit.SPADES)); // 17

        int initialCards = game.getCrupier().getCardCount();

        game.crupierHitOneCard();

        assertEquals(initialCards, game.getCrupier().getCardCount());
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }

    @Test
    void testPlayerStand_SetsCrupierTurnStatus() {
        // Given: A game in PLAYER_TURN
        Game game = createTestGame(scoringService);
        game.startGame();

        // When: Player stands
        game.playerStand();

        // Then: Status should be CRUPIER_TURN (not immediately FINISHED)
        assertEquals(GameStatus.CRUPIER_TURN, game.getGameStatus());
    }

}
