package com.itacademy.blackjack;

import com.itacademy.blackjack.deck.model.Deck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlackjackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlackjackApplication.class, args);

		Deck deck = new Deck();
	//	System.out.println(deck.getCardsSnapshot());
		System.out.println(deck.draw());
	}

}
