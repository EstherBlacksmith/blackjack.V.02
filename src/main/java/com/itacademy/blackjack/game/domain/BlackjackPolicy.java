package com.itacademy.blackjack.game.domain;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.game.model.GameResult;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlackjackPolicy {


     //Checks if a hand is a blackjack (21 on first 2 cards)

    public boolean isBlackjack(List<Card> hand, int score) {
        return score == 21 && hand.size() == 2;
    }


    //Determines the result when player has blackjack

    public GameResult determineBlackjackResult(
            List<Card> playerHand, int playerScore,
            List<Card> crupierHand, int crupierScore) {

        if (!isBlackjack(playerHand, playerScore)) {
            throw new IllegalStateException("Player does not have blackjack");
        }

        // If crupier also has blackjack, it's a push
        if (isBlackjack(crupierHand, crupierScore)) {
            return GameResult.PUSH;
        }

        return GameResult.BLACKJACK;
    }

}
