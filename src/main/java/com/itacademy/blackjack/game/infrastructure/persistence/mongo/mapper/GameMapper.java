package com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.domain.model.CardData;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    private static List<CardData> toCardDataList(List<GameDocument.CardDocument> cardDocuments) {
        if (cardDocuments == null) return List.of();
        return cardDocuments.stream()
                .map(doc -> new CardData(doc.getRank(), doc.getSuit(), doc.getValue()))
                .collect(Collectors.toList());
    }

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

    // GameDocument (MongoDB) → Game (dominio)
    public Game toDomain(GameDocument document) {
        return Game.reconstruct(
                document.getId(),
                document.getPlayerId(),
                document.getPlayerName(),
                toCardDataList(document.getPlayerCards()),
                toCardDataList(document.getCrupierCards()),
                document.getGameStatus(),
                document.getGameResult()
        );
    }

    // Card (enum) → CardDocument (String)
    private List<GameDocument.CardDocument> toCardDocumentList(List<Card> cards) {
        return cards.stream()
                .map(card -> GameDocument.CardDocument.builder()
                        .rank(card.getRank().name())
                        .suit(card.getSuit().name())
                        .value(card.getNumericValue())
                        .build())
                .collect(Collectors.toList());
    }

    // CardDocument (String) → Card (enum)
    private List<Card> toCardList(List<GameDocument.CardDocument> cardDocuments) {
        if (cardDocuments == null) return List.of();
        return cardDocuments.stream()
                .map(doc -> new Card(
                        CardRank.valueOf(doc.getRank()),
                        Suit.valueOf(doc.getSuit())
                ))
                .collect(Collectors.toList());
    }
}
