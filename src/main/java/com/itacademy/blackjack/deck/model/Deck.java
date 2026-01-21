package com.itacademy.blackjack.deck.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public record Deck(List<Card> cards) {

    public Deck() {
        this(new ArrayList<>());
        initialize();
        shuffle();
    }

    private void initialize() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (CardRank cardRank : CardRank.values()) {
                cards.add(new Card(cardRank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("The deck is empty");
        }
        return cards.removeLast();
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void reset() {
        initialize();
        shuffle();
    }

    public List<Card> getCardsSnapshot() {
        return List.copyOf(cards);
    }
}
