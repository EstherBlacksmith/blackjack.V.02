package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.deck.model.ScoringService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private UUID id;

    @Setter
    @Getter
    private GameStatus gameStatus;
    @Setter
    @Getter
    private Deck deck;
    @Setter
    @Getter
    private List<Card> playerHand;
    @Setter
    @Getter
    private List<Card> crupierHand;
    @Setter
    @Getter
    private GameResult gameResult;

    private final ScoringService scoringService = new ScoringService();

    public Game() {
        this.id = UUID.randomUUID();
        this.gameStatus = GameStatus.CREATED;
        this.deck = new Deck();
        this.playerHand = new ArrayList<>();
        this.crupierHand = new ArrayList<>();
        this.gameResult = null;
    }

    public Card drawCardFromDeck() {
        return deck.draw();
    }

    public void dealInitialCards() {
        // Deal 2 cards to player
        playerHand.add(drawCardFromDeck());
        playerHand.add(drawCardFromDeck());

        // Deal 2 cards to crupier
        crupierHand.add(drawCardFromDeck());
        crupierHand.add(drawCardFromDeck());
    }

    public void playerHit() {
        if (gameStatus != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        playerHand.add(drawCardFromDeck());
    }

    public void crupierHit() {
        crupierHand.add(drawCardFromDeck());
    }

    public int getPlayerScore() {
        return scoringService.calculateHandScore(playerHand);
    }

    public int getCrupierScore() {
        return scoringService.calculateHandScore(crupierHand);
    }
}
