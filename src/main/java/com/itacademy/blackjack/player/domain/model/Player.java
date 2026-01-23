package com.itacademy.blackjack.player.domain.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;

import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.Hand;
import com.itacademy.blackjack.game.domain.model.PlayerStatus;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Entity representing a player in the Blackjack game.
 *
 * Entity Characteristics:
 * - Has a unique identity (id) that persists across state changes
 * - Has a hand (value object) and status (enum)
 * - Can change state through defined actions
 */
@Table("players")
@Getter
public class Player {
    @Id
    private final UUID id;
    private final String name;
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
