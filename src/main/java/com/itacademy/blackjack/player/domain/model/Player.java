package com.itacademy.blackjack.player.domain.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.domain.model.CardData;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.Hand;
import com.itacademy.blackjack.game.domain.model.PlayerStatus;
import lombok.Getter;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;

/**
 * Entity representing a player in the Blackjack game.
 * <p>
 * Entity Characteristics:
 * - Has a unique identity (id) that persists across state changes
 * - Has a hand (value object) and status (enum)
 * - Can change state through defined actions
 */


@Getter
public class Player {
    @Id
    private UUID id;
    private String name;
    private Hand hand;
    private PlayerStatus status;
    private int wins;
    private int losses;
    private int pushes;

    public Player(String name, ScoringService scoringService) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.hand = new Hand(scoringService);
        this.status = PlayerStatus.ACTIVE;
        this.wins = 0;
        this.losses = 0;
        this.pushes = 0;
    }

    // Constructor for the factory methods
    private Player() {
        this.hand = new Hand(new ScoringService());
    }

    //  Factory method for the reconstruction from MYSQL
    public static Player fromDatabase(UUID id, String name, int wins, int losses, int pushes) {
        Player player = new Player();
        player.id = id;
        player.name = name;
        player.wins = wins;
        player.losses = losses;
        player.pushes = pushes;
        player.status = PlayerStatus.ACTIVE;

        // Hand is empty for the new games
        return player;
    }

    // Factory method public
    public static Player createNew(String name, ScoringService scoringService) {
        return new Player(name, scoringService);
    }

    public static Player reconstruct(UUID id, String name, List<CardData> cards) {
        Player player = new Player();
        player.id = id;
        player.name = name;
        player.status = PlayerStatus.ACTIVE;

        // Add cards to hand
        for (CardData cardDoc : cards) {
            Card card = new Card(
                    CardRank.valueOf(cardDoc.rank()),
                    Suit.valueOf(cardDoc.suit())
            );
            player.hand.addCard(card);
        }

        return player;
    }

    public void receiveCard(Card card) {
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Player " + name + " cannot receive cards in status: " + status
            );
        }
        hand.addCard(card);
        updateStatus();
    }

    private void updateStatus() {
        if (hand.isBlackjack()) {
            status = PlayerStatus.BLACKJACK;
        } else if (hand.isBusted()) {
            status = PlayerStatus.BUSTED;
        }
    }

    public void stand() {
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Player " + name + " cannot stand in status: " + status
            );
        }
        status = PlayerStatus.STOOD;
    }

    public int getScore() {
        return hand.getScore();
    }

    public boolean isActive() {
        return status == PlayerStatus.ACTIVE;
    }

    public void applyGameResult(GameResult result) {
        switch (result) {
            case PLAYER_WINS:
            case BLACKJACK:
                this.wins++;
                break;
            case CRUPIER_WINS:
                this.losses++;
                break;
            case PUSH:
                this.pushes++;
                break;
            case NO_RESULTS_YET:
            default:
                break;
        }
    }

}
