package com.itacademy.blackjack.player.domain.model;

import com.itacademy.blackjack.deck.infrastructure.CardMapper;
import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.domain.model.CardData;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.Hand;
import com.itacademy.blackjack.game.domain.model.PlayerStatus;
import lombok.Builder;
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

    private final UUID id;
    private final String name;

    private int wins;
    private int losses;
    private int pushes;
    private Hand hand;
    private PlayerStatus status;

    //  PRIVATE CONSTRUCTOR (Only Builder can create)
    private Player(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.wins = builder.wins;
        this.losses = builder.losses;
        this.pushes = builder.pushes;
        this.hand = builder.hand != null ? builder.hand : new Hand();
        this.status = builder.status != null ? builder.status : PlayerStatus.ACTIVE;
    }

    //  CUSTOM BUILDER
    public static class Builder {
        private UUID id;
        private String name;
        private int wins;
        private int losses;
        private int pushes;

        private Hand hand = null;
        private PlayerStatus status = null;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }


        public Builder wins(int wins) {
            this.wins = wins;
            return this;
        }


        public Builder losses(int losses) {
            this.losses = losses;
            return this;
        }


        public Builder pushes(int pushes) {
            this.pushes = pushes;
            return this;
        }

        // Optional: hand
        public Builder hand(Hand hand) {
            this.hand = hand;
            return this;
        }


        public Builder status(PlayerStatus status) {
            this.status = status;
            return this;
        }

        // Build method
        public Player build() {
            // Auto-generate id if not provided
            UUID finalId = (id != null) ? id : UUID.randomUUID();

            return new Player(this);
        }
    }

    // Static factory method to start building
    public static Builder builder() {
        return new Builder();
    }

    // FACTORY METHODS

    public static Player createNew(String name) {
        // Trust the name is already validated by the DTO

        return Player.builder()
                .id(UUID.randomUUID())
                .name(name)
                .wins(0)
                .losses(0)
                .pushes(0)
                .hand(new Hand())
                .status(PlayerStatus.ACTIVE)
                .build();
    }

    public static Player fromDatabase(UUID id, String name, int wins, int losses, int pushes) {
        return Player.builder()
                .id(id)
                .name(name)
                .wins(wins)
                .losses(losses)
                .pushes(pushes)
                .status(PlayerStatus.ACTIVE)
                .build();
    }

    public static Player reconstruct(UUID id, String name, List<CardData> cards) {
        Builder builder = Player.builder()
                .id(id)
                .name(name)
                .status(PlayerStatus.ACTIVE);

        if (cards != null && !cards.isEmpty()) {
            Hand hand = new Hand();
            for (CardData cardDoc : cards) {
                Card card = CardMapper.toCard(cardDoc);
                hand.addCard(card);
            }
            builder.hand(hand);
        }

        return builder.build();
    }

    // DOMAIN METHODS

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

    public PlayerStats getStats() {
        return new PlayerStats(wins, losses, pushes);
    }
}