package com.itacademy.blackjack.deck.model;


public record Card(CardRank cardRank, Suit suit) {

    public Card(CardRank cardRank, Suit suit) {
        this.cardRank = cardRank;
        this.suit = suit;
    }

    public CardRank getRank() {
        return this.cardRank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getNumericValue() {
        return cardRank.getNumericValue();
    }
}
