package com.itacademy.blackjack;

import com.itacademy.blackjack.deck.model.Deck;
import com.itacademy.blackjack.deck.model.ScoringService;

import com.itacademy.blackjack.game.domain.model.Game;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlackjackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlackjackApplication.class, args);

		Deck deck = new Deck();
	//	System.out.println(deck.getCardsSnapshot());
		System.out.println(deck.draw());

		ScoringService scoringService = new ScoringService();

		Game game = new Game(scoringService);
		game.dealInitialCards();
		System.out.println(game.getPlayer().getHand());
		System.out.println(game.getCrupier().getHand());
		System.out.println(game.getCrupier().getScore());
		System.out.println(game.getPlayer().getScore());
		game.determineWinner();
	}

}
