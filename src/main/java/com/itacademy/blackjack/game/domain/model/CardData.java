package com.itacademy.blackjack.game.domain.model;

public record CardData(String rank, String suit, int value) {
    public CardData {
        if (rank == null || rank.isBlank()) {
            throw new IllegalArgumentException("Card rank cannot be null or blank");
        }
        if (suit == null || suit.isBlank()) {
            throw new IllegalArgumentException("Card suit cannot be null or blank");
        }
        if (value < 1 || value > 11) {
            throw new IllegalArgumentException("Card value must be between 1 and 11");
        }
    }
}

