package com.itacademy.blackjack.deck.model;


import com.itacademy.blackjack.game.domain.model.CardData;

public record Card(CardRank cardRank, Suit suit) {

    public Card(CardRank cardRank, Suit suit) {
        this.cardRank = cardRank;
        this.suit = suit;
    }

    public static Card fromData(CardData data) {
        return new Card(
                CardRank.valueOf(data.rank()),
                Suit.valueOf(data.suit())
        );
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
