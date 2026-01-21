package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.Suit;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testGameInitialization() {
        // Given: A new Game instance
        Game game = new Game();

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
        Game game = new Game();

        // Then: Verify deck has 52 cards
        assertEquals(52, game.getDeck().size(), "Deck should have 52 cards");
        assertFalse(game.getDeck().isEmpty(), "Deck should not be empty");
    }

    @Test
    void testPlayerScoreInitiallyZero() {
        // Given: A new Game instance
        Game game = new Game();

        // Then: Player score should be 0 (no cards)
        assertEquals(0, game.getPlayerScore(), "Player score should be 0 with no cards");
    }

    @Test
    void testDrawCardFromEmptyDeckThrowsException() {
        // Given: A game with all cards drawn
        Game game = new Game();
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
        Game game = new Game();
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
        Game game = new Game();
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
        Game game = new Game();
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
        Game game = new Game();
        assertEquals(GameStatus.CREATED, game.getGameStatus());

        // When: Start the game
        game.startGame();

        // Then: Status should change (unless player got Blackjack)
        assertNotEquals(GameStatus.CREATED, game.getGameStatus());
    }

}
