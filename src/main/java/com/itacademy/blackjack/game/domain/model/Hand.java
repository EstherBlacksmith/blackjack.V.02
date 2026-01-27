package com.itacademy.blackjack.game.domain.model;

import com.itacademy.blackjack.deck.model.Card;

import com.itacademy.blackjack.deck.model.ScoringService;

import java.util.ArrayList;
import java.util.List;

public final class Hand {
    private final List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards); // Defensive copy
    }

    // Immutable access
    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public int getScore() {
        return ScoringService.calculateHandScore(cards);  // Static call
    }

    public int getCardCount() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public boolean isBlackjack() {
        return getScore() == 21 && cards.size() == 2;  // ✓ No changes needed
    }

    public boolean isBusted() {
        return getScore() > 21;  // ✓ No changes needed
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return cards.equals(hand.cards);
    }

    @Override
    public int hashCode() {
        return cards.hashCode();
    }
}
