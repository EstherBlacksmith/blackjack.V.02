package com.itacademy.blackjack.game.domain.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;

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
}
