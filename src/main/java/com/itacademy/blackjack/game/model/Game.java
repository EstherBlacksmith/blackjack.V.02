package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.deck.model.ScoringService;

import com.itacademy.blackjack.game.model.exception.NotPlayerTurnException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.UUID;

@Slf4j
public class Game {
    @Getter
    private UUID id;

    @Setter
    @Getter
    private GameStatus gameStatus;
    @Setter
    @Getter
    private Deck deck;
    @Getter
    private final Player player;
    @Getter
    private final Crupier crupier;
    @Setter
    @Getter
    private GameResult gameResult;

    private final ScoringService scoringService ;

    public Game(ScoringService scoringService) {
        this.id = UUID.randomUUID();
        this.gameStatus = GameStatus.CREATED;
        this.deck = new Deck();
        this.player = new Player("Player", scoringService);
        this.crupier = new Crupier(scoringService);
        this.gameResult = GameResult.NO_RESULTS_YET;
        this.scoringService = scoringService;
    }

    public Card drawCardFromDeck() {
        return deck.draw();
    }

    public void dealInitialCards() {
        //2 initial card for each player (player and crupier)
        player.receiveCard(drawCardFromDeck());
        crupier.receiveCard(drawCardFromDeck());
        player.receiveCard(drawCardFromDeck());
        crupier.receiveCard(drawCardFromDeck());
    }

    public void startGame() {
        dealInitialCards();

        // Check for immediate Blackjack
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

        // Dealer must hit on 16 or less, stand on 17 or more
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



    public void determineWinner(){
        int playerScore = player.getScore();
        int crupierScore = crupier.getScore();


      /*  Player Blackjack → Player wins 3:2 (unless crupier also has Blackjack = push)
        Player Busts → Player loses (crupier wins)
        Crupier Busts → Crupier loses (player wins)
        Player Score > Crupier Score → Player wins
        Crupier Score > Player Score → Crupier wins
        Scores Equal → Push (tie, nobody wins)*/

        // Apply Blackjack rules
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

        log.info("Game ended. Player: {}, Crupier: {}, Result: {}",
                playerScore, crupierScore, gameResult);

        gameStatus = GameStatus.FINISHED;
    }

    public void playerStand() {
        if (gameStatus != GameStatus.PLAYER_TURN) {
            throw new NotPlayerTurnException("It's not your turn!");
        }

        player.stand();
        log.info("Player stood with score: {}", player.getScore());
        gameStatus = GameStatus.CRUPIER_TURN;
        crupierTurn();
    }

}
