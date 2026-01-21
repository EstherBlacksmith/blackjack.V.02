package com.itacademy.blackjack.game.model;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.BlackjackPolicy;
import com.itacademy.blackjack.game.model.exception.NotPlayerTurnException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/*
* ✅ Completed:

Create BlackjackPolicy domain service
Create BlackjackPolicyTest
Update Game.java to use BlackjackPolicy
Update GameTest.java with BlackjackPolicy dependency
🔲 Pending:
5. Remove/fix blackjack tests in GameTest (testing private methods)
6. Create GameRepository
7. Create DTOs (GameResponse, CardResponse)
8. Update GameController with REST endpoints
9. Run all tests and fix failures
* */
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
    @Setter
    @Getter
    private List<Card> playerHand;
    @Setter
    @Getter
    private List<Card> crupierHand;
    @Setter
    @Getter
    private GameResult gameResult;

    private final ScoringService scoringService ;
    private final BlackjackPolicy blackjackPolicy;

    public Game(ScoringService scoringService, BlackjackPolicy blackjackPolicy) {
        this.id = UUID.randomUUID();
        this.gameStatus = GameStatus.CREATED;
        this.deck = new Deck();
        this.playerHand = new ArrayList<>();
        this.crupierHand = new ArrayList<>();
        this.gameResult = null;
        this.scoringService = scoringService;
        this.blackjackPolicy = blackjackPolicy;
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

    public void startGame() {
        dealInitialCards();
        this.gameStatus = GameStatus.STARTED;

        int playerScore = getPlayerScore();
        int crupierScore = getCrupierScore();

        if (blackjackPolicy.isBlackjack(playerHand, playerScore)) {
            gameResult = blackjackPolicy.determineBlackjackResult(
                    playerHand, playerScore, crupierHand, crupierScore
            );
            gameStatus = GameStatus.FINISHED;
        } else {
            this.gameStatus = GameStatus.PLAYER_TURN;
        }
    }


    private boolean isBlackjack() {
        return getPlayerScore() == 21 && playerHand.size() == 2;
    }

    private void handleBlackjack() {
        if (getCrupierScore() == 21) {
            gameResult = GameResult.PUSH;
        } else {
            gameResult = GameResult.BLACKJACK;
        }
        gameStatus = GameStatus.FINISHED;
    }

    public void crupierTurn() {
        gameStatus = GameStatus.CRUPIER_TURN;

        while (getCrupierScore() <= 16) {
            crupierHit();
        }

        determineWinner();
    }

    public void playerHit() {
        if (gameStatus != GameStatus.PLAYER_TURN) {
            throw new NotPlayerTurnException("It's not your turn!");
        }
        playerHand.add(drawCardFromDeck());

        if (getPlayerScore() > 21) {
            gameResult = GameResult.CRUPIER_WINS;
            gameStatus = GameStatus.FINISHED;
        }
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

    public void determineWinner(){
        int playerScore = getPlayerScore();
        int crupierScore = getCrupierScore();


      /*  Player Blackjack → Player wins 3:2 (unless crupier also has Blackjack = push)
        Player Busts → Player loses (crupier wins)
        Crupier Busts → Crupier loses (player wins)
        Player Score > Crupier Score → Player wins
        Crupier Score > Player Score → Crupier wins
        Scores Equal → Push (tie, nobody wins)*/

        // Check for busts FIRST!
        if (playerScore > 21) {
            gameResult = GameResult.CRUPIER_WINS;  // Player busted
        } else if (crupierScore > 21) {
            gameResult = GameResult.PLAYER_WINS;   // Crupier busted
        } else if (playerScore > crupierScore) {
            gameResult = GameResult.PLAYER_WINS;
        } else if (crupierScore > playerScore) {
            gameResult = GameResult.CRUPIER_WINS;
        } else {
            gameResult = GameResult.PUSH;
        }

        log.info("Player: {}, Crupier: {}", playerScore, crupierScore);

        gameStatus = GameStatus.FINISHED;

    }

    public void playerStand(){
        if (gameStatus != GameStatus.PLAYER_TURN) {
            throw new NotPlayerTurnException("It's not your turn!");
        }
        this.gameStatus = GameStatus.CRUPIER_TURN;
        crupierTurn();
    }





}
