package com.itacademy.blackjack.deck.model;

import java.util.List;

public class ScoringService {
    public int calculateHandScore(List<Card> hand) {
        int score = hand.stream().mapToInt(Card::getNumericValue).sum();
        // Ace logic: if score <= 11, count Ace as 11
        boolean hasAce = hand.stream().anyMatch(c -> c.getRank() == CardRank.ACE);
        if (hasAce && score <= 11) {
            score += 10; // Add 10 to count Ace as 11 instead of 1
        }
        return score;
    }
}