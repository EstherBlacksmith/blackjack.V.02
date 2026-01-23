package com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    // Game (dominio) → GameDocument (MongoDB)
    public GameDocument toDocument(Game game) {
        return GameDocument.builder()
                .id(game.getId().toString())
                .playerId(game.getPlayer().getId().toString())
                .playerName(game.getPlayer().getName())
                .playerCards(toCardDocumentList(game.getPlayer().getHand().getCards()))
                .playerScore(game.getPlayer().getScore())
                .crupierCards(toCardDocumentList(game.getCrupier().getHand().getCards()))
                .crupierScore(game.getCrupier().getScore())
                .gameStatus(game.getGameStatus())
                .gameResult(game.getGameResult())
                .build();
    }

    // Card (enum) → CardDocument (String)
    private List<GameDocument.CardDocument> toCardDocumentList(List<Card> cards) {
        return cards.stream()
                .map(card -> GameDocument.CardDocument.builder()
                        .rank(card.getRank().name())      // TWO → "TWO"
                        .suit(card.getSuit().name())      // DIAMONDS → "DIAMONDS"
                        .value(card.getNumericValue())
                        .build())
                .collect(Collectors.toList());
    }
}
