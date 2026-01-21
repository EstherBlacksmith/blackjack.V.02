package com.itacademy.blackjack.game.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.model.GameResult;
import org.junit.jupiter.api.Test;

import java.util.List;

class BlackjackPolicyTest {

    private BlackjackPolicy policy = new BlackjackPolicy();

    @Test
    void testIsBlackjackReturnsTrueForAceAndFaceCard() {
        List<Card> hand = List.of(
                new Card(CardRank.ACE, Suit.HEARTS),
                new Card(CardRank.JACK, Suit.SPADES)
        );

        assertTrue(policy.isBlackjack(hand, 21));
    }

    @Test
    void testIsBlackjackReturnsFalseFor21With3Cards() {
        List<Card> hand = List.of(
                new Card(CardRank.TEN, Suit.HEARTS),
                new Card(CardRank.SIX, Suit.SPADES),
                new Card(CardRank.FIVE, Suit.CLUBS)
        );

        assertFalse(policy.isBlackjack(hand, 21));
    }

    @Test
    void testBlackjackVsBlackjackReturnsPush() {
        List<Card> playerHand = List.of(
                new Card(CardRank.ACE, Suit.HEARTS),
                new Card(CardRank.JACK, Suit.SPADES)
        );

        List<Card> crupierHand = List.of(
                new Card(CardRank.ACE, Suit.CLUBS),
                new Card(CardRank.KING, Suit.DIAMONDS)
        );

        GameResult result = policy.determineBlackjackResult(
                playerHand, 21, crupierHand, 21
        );

        assertEquals(GameResult.PUSH, result);
    }

    @Test
    void testBlackjackVsNonBlackjackReturnsBlackjack() {
        List<Card> playerHand = List.of(
                new Card(CardRank.ACE, Suit.HEARTS),
                new Card(CardRank.JACK, Suit.SPADES)
        );

        List<Card> crupierHand = List.of(
                new Card(CardRank.TEN, Suit.CLUBS),
                new Card(CardRank.SIX, Suit.DIAMONDS)
        );

        GameResult result = policy.determineBlackjackResult(
                playerHand, 21, crupierHand, 16
        );

        assertEquals(GameResult.BLACKJACK, result);
    }
}
