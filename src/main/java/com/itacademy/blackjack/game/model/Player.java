package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;
import lombok.Getter;

import java.util.UUID;

/**
 * Entity representing a player in the Blackjack game.
 *
 * Entity Characteristics:
 * - Has a unique identity (id) that persists across state changes
 * - Has a hand (value object) and status (enum)
 * - Can change state through defined actions
 */
@Getter
public class Player {
    private final UUID id;
    private final String name;
    private Hand hand;
    private PlayerStatus status;

    public Player(String name, ScoringService scoringService) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.hand = new Hand(scoringService);
        this.status = PlayerStatus.ACTIVE;
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

    public boolean canAct() {
        return status == PlayerStatus.ACTIVE;
    }
}
