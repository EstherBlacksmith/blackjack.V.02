package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.model.exception.NotPlayerTurnException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    ScoringService scoringService ;
    @BeforeEach
    public void setUp(){
         scoringService = new ScoringService();
    }
    @Test
    void testGameInitialization() {
        // Given: A new Game instance
        Game game = new Game(scoringService);

        // Then: Verify all initial conditions
        assertNotNull(game.getId(), "Game ID should not be null");
        assertNotNull(game.getDeck(), "Deck should be initialized");
        assertEquals(GameStatus.CREATED, game.getGameStatus(), "Initial status should be CREATED");
        assertTrue(game.getPlayerHand().isEmpty(), "Player hand should be empty");
        assertTrue(game.getCrupierHand().isEmpty(), "Crupier hand should be empty");
        assertNull(game.getGameResult(), "Game result should be null initially");
    }

    @Test
    void testDeckIsShuffledOnInitialization() {
        // Given: A new Game instance
        Game game = new Game(scoringService);


        // Then: Verify deck has 52 cards
        assertEquals(52, game.getDeck().size(), "Deck should have 52 cards");
        assertFalse(game.getDeck().isEmpty(), "Deck should not be empty");
    }

    @Test
    void testPlayerScoreInitiallyZero() {
        // Given: A new Game instance
        Game game = new Game(scoringService);


        // Then: Player score should be 0 (no cards)
        assertEquals(0, game.getPlayerScore(), "Player score should be 0 with no cards");
    }

    @Test
    void testDrawCardFromEmptyDeckThrowsException() {
        // Given: A game with all cards drawn
        Game game = new Game(scoringService);

        for (int i = 0; i < 52; i++) {
            game.drawCardFromDeck();
        }

        // When/Then: Drawing from empty deck should throw exception
        assertThrows(NoSuchElementException.class, () -> {
            game.drawCardFromDeck();
        }, "Should throw exception when drawing from empty deck");
    }

    @Test
    void testCrupierWinsWhenPlayerBusts() {
        // Given: A game where we manually set up a bust scenario
        Game game = new Game(scoringService);
        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayerHand().add(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!

        game.getCrupierHand().add(new Card(CardRank.SIX, Suit.HEARTS));
        game.getCrupierHand().add(new Card(CardRank.FIVE, Suit.SPADES));

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.CRUPIER_WINS, game.getGameResult());
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }

    @Test
    void testPlayerWinsWhenCrupierBusts() {
        // Given: A game where crupier busts
        Game game = new Game(scoringService);
        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.SIX, Suit.SPADES)); // 16

        game.getCrupierHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getCrupierHand().add(new Card(CardRank.KING, Suit.SPADES));
        game.getCrupierHand().add(new Card(CardRank.FIVE, Suit.CLUBS)); // Bust!

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.PLAYER_WINS, game.getGameResult());
    }

    @Test
    void testPushWhenScoresAreEqual() {
        // Given: A tie game
        Game game = new Game(scoringService);

        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.SIX, Suit.SPADES)); // 16

        game.getCrupierHand().add(new Card(CardRank.KING, Suit.CLUBS));
        game.getCrupierHand().add(new Card(CardRank.SIX, Suit.DIAMONDS)); // 16

        // When
        game.determineWinner();

        // Then
        assertEquals(GameResult.PUSH, game.getGameResult());
    }

    @Test
    void testGameStatusTransitions() {
        // Given: A new game
        Game game = new Game(scoringService);

        assertEquals(GameStatus.CREATED, game.getGameStatus());

        // When: Start the game
        game.startGame();

        // Then: Status should change (unless player got Blackjack)
        assertNotEquals(GameStatus.CREATED, game.getGameStatus());
    }

    @Test
    void testPlayerHitThrowsExceptionWhenNotPlayerTurn() {
        Game game = new Game(scoringService);

        game.startGame();

        // Simulate game is finished
        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.KING, Suit.SPADES));
        game.getPlayerHand().add(new Card(CardRank.TWO, Suit.CLUBS)); // Bust!
        game.determineWinner();

        // Then: hit should throw exception
        assertThrows(NotPlayerTurnException.class, () -> {
            game.playerHit();
        });
    }

    @Test
    void testPlayerStandSwitchesToCrupierTurn() {
        Game game = new Game(scoringService);

        game.startGame();

        // When: Player stands
        game.playerStand();

        // Then: Game should be finished (crupier played)
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }


    @Test
    void testPlayerWinsWithHigherScore() {
        Game game = new Game(scoringService);

        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.NINE, Suit.SPADES)); // 19
        game.getCrupierHand().add(new Card(CardRank.TEN, Suit.CLUBS));
        game.getCrupierHand().add(new Card(CardRank.SIX, Suit.DIAMONDS)); // 16

        game.determineWinner();

        assertEquals(GameResult.PLAYER_WINS, game.getGameResult());
    }

    @Test
    void testCrupierWinsWithHigherScore() {
        Game game = new Game(scoringService);

        game.getPlayerHand().add(new Card(CardRank.TEN, Suit.HEARTS));
        game.getPlayerHand().add(new Card(CardRank.FIVE, Suit.SPADES)); // 15
        game.getCrupierHand().add(new Card(CardRank.TEN, Suit.CLUBS));
        game.getCrupierHand().add(new Card(CardRank.SEVEN, Suit.DIAMONDS)); // 17

        game.determineWinner();

        assertEquals(GameResult.CRUPIER_WINS, game.getGameResult());
    }
}
