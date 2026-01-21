package com.itacademy.blackjack;

import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.game.model.Game;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlackjackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlackjackApplication.class, args);

		Deck deck = new Deck();
	//	System.out.println(deck.getCardsSnapshot());
		System.out.println(deck.draw());

		Game game = new Game();
		game.dealInitialCards();
		System.out.println(game.getPlayerHand());
		System.out.println(game.getCrupierHand());
		System.out.println(game.getCrupierScore());
		System.out.println(game.getPlayerScore());
		game.determineWinner();
	}

}
