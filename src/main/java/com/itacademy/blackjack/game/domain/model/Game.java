package com.itacademy.blackjack.game.domain.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.exception.NotPlayerTurnException;
import com.itacademy.blackjack.player.domain.model.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class Game {

    // Required fields (final)
    @Getter
    private final UUID id;
    // Optional fields (can be modified)
    @Getter
    private GameStatus gameStatus;
    @Getter
    private GameResult gameResult;
    @Getter
    private Deck deck;
    @Getter
    private Player player;
    @Getter
    private Crupier crupier;

    // Private constructor - only Builder can create instances
    private Game(Builder builder) {
        this.id = builder.id;
        this.gameStatus = builder.gameStatus;
        this.gameResult = builder.gameResult;
        this.deck = builder.deck;
        this.player = builder.player;
        this.crupier = builder.crupier;
    }

    public int getPlayerScore() {
        return ScoringService.calculateHandScore(player.getHand().getCards());
    }

    // ========== CUSTOM BUILDER ==========
    public static class Builder {
        private UUID id;
        private GameStatus gameStatus = GameStatus.CREATED;
        private GameResult gameResult = GameResult.NO_RESULTS_YET;
        private Deck deck = new Deck();
        private Player player;
        private Crupier crupier;

        // Required: id
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        // Optional fields with defaults
        public Builder gameStatus(GameStatus gameStatus) {
            this.gameStatus = gameStatus;
            return this;
        }

        public Builder gameResult(GameResult gameResult) {
            this.gameResult = gameResult;
            return this;
        }

        public Builder deck(Deck deck) {
            this.deck = deck;
            return this;
        }

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder crupier(Crupier crupier) {
            this.crupier = crupier;
            return this;
        }

        // Validation before building
        public Game build() {
            // Validate required fields
            if (id == null) {
                throw new IllegalStateException("Game ID is required");
            }

            if (player == null) {
                throw new IllegalStateException("Player is required");
            }
            if (crupier == null) {
                crupier = new Crupier();
            }
            if (deck == null) {
                deck = new Deck();
            }

            return new Game(this);
        }
    }

    // Static factory method to start building
    public static Builder builder() {
        return new Builder();
    }

    // ========== DOMAIN METHODS ==========

    public Card drawCardFromDeck() {
        return deck.draw();
    }

    public void dealInitialCards() {
        player.receiveCard(drawCardFromDeck());
        crupier.receiveCard(drawCardFromDeck());
        player.receiveCard(drawCardFromDeck());
        crupier.receiveCard(drawCardFromDeck());
    }

    public void startGame() {
        dealInitialCards();

        if (player.getStatus() == PlayerStatus.BLACKJACK) {
            handleBlackjack();
        } else {
            gameStatus = GameStatus.PLAYER_TURN;
        }
    }

    private void handleBlackjack() {
        if (crupier.hasBlackjack()) {
            gameResult = GameResult.PUSH;
        } else {
            gameResult = GameResult.BLACKJACK;
        }
        gameStatus = GameStatus.FINISHED;
        log.info("Player has Blackjack! Result: {}", gameResult);
    }

    public void crupierTurn() {
        log.info("Crupier turn starting. Score: {}", crupier.getScore());

        while (crupier.mustHit()) {
            Card card = drawCardFromDeck();
            crupier.receiveCard(card);
            log.debug("Crupier drew: {}. New score: {}", card, crupier.getScore());
        }

        log.info("Crupier stands with score: {}", crupier.getScore());
        determineWinner();
    }

    public void playerHit() {
        if (gameStatus != GameStatus.PLAYER_TURN) {
            throw new NotPlayerTurnException("It's not your turn!");
        }

        Card card = drawCardFromDeck();
        player.receiveCard(card);
        log.debug("Player drew: {}", card);

        if (player.getStatus() == PlayerStatus.BUSTED) {
            log.info("Player busted with score: {}", player.getScore());
            gameResult = GameResult.CRUPIER_WINS;
            gameStatus = GameStatus.FINISHED;
        }
    }

    public void determineWinner() {
        int playerScore = player.getScore();
        int crupierScore = crupier.getScore();

        if (player.getStatus() == PlayerStatus.BUSTED) {
            gameResult = GameResult.CRUPIER_WINS;
        } else if (crupier.isBusted()) {
            gameResult = GameResult.PLAYER_WINS;
        } else if (playerScore > crupierScore) {
            gameResult = GameResult.PLAYER_WINS;
        } else if (crupierScore > playerScore) {
            gameResult = GameResult.CRUPIER_WINS;
        } else {
            gameResult = GameResult.PUSH;
        }

        updatePlayerStatistics();

        log.info("Game ended. Player: {}, Crupier: {}, Result: {}",
                playerScore, crupierScore, gameResult);

        gameStatus = GameStatus.FINISHED;
    }

    private void updatePlayerStatistics() {
        player.applyGameResult(gameResult);
    }

    public void playerStand() {
        if (gameStatus != GameStatus.PLAYER_TURN) {
            throw new NotPlayerTurnException("It's not your turn!");
        }

        player.stand();
        log.info("Player stood with score: {}", player.getScore());
        gameStatus = GameStatus.CRUPIER_TURN;
    }

    public void crupierHitOneCard() {
        if (gameStatus != GameStatus.CRUPIER_TURN) {
            throw new NotPlayerTurnException("Not crupier turn!");
        }

        if (crupier.mustHit()) {
            Card card = drawCardFromDeck();
            crupier.receiveCard(card);
            log.debug("Crupier drew: {}. New score: {}", card, crupier.getScore());
        }

        if (!crupier.mustHit()) {
            determineWinner();
        }
    }

    // ========== RECONSTRUCT METHOD ==========

    public static Game reconstruct(
            String id,
            String playerId,
            String playerName,
            List<CardData> playerCards,
            List<CardData> crupierCards,
            GameStatus gameStatus,
            GameResult gameResult
    ) {
        Player player = Player.reconstruct(
                UUID.fromString(playerId),
                playerName,
                playerCards
        );

        Crupier crupier = Crupier.reconstruct(crupierCards);

        return Game.builder()
                .id(UUID.fromString(id))
                .gameStatus(gameStatus)
                .gameResult(gameResult)
                .deck(new Deck())
                .player(player)
                .crupier(crupier)
                .build();
    }

}
