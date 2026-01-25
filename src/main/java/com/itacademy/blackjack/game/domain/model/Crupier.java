package com.itacademy.blackjack.game.domain.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.deck.model.Suit;

import java.util.List;

/**
 * Simple class representing the dealer (Crupier) in Blackjack.
 */
public class Crupier {
    private final Hand hand;

    public Crupier(ScoringService scoringService) {
        this.hand = new Hand(scoringService);
    }

    public void receiveCard(Card card) {
        hand.addCard(card);
    }

    public int getScore() {
        return hand.getScore();
    }

    public int getCardCount() {
        return hand.getCardCount();
    }

    public boolean mustHit() {
        return hand.getScore() <= 16;
    }

    public boolean mustStand() {
        return hand.getScore() >= 17;
    }

    public boolean isBusted() {
        return hand.isBusted();
    }

    public boolean hasBlackjack() {
        return hand.isBlackjack();
    }

    public Hand getHand() {
        return hand;
    }

    public static Crupier reconstruct(List<CardData> cards) {
        Crupier crupier = new Crupier(new ScoringService());
        for (CardData cardDoc : cards) {
            Card card = new Card(
                    CardRank.valueOf(cardDoc.rank()),
                    Suit.valueOf(cardDoc.suit())
            );
            crupier.receiveCard(card);
        }
        return crupier;
    }

}
