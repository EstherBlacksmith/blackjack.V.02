package com.itacademy.blackjack.game.service;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.dto.CardResponse;
import com.itacademy.blackjack.game.dto.GameResponse;
import com.itacademy.blackjack.game.dto.PlayerResponse;
import com.itacademy.blackjack.game.model.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final ScoringService scoringService;

    public GameService(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    public Mono<GameResponse> startNewGame(UUID playerId) {
        Game game = new Game(scoringService);
        game.startGame();

        GameResponse response = mapToResponse(game);
        return Mono.just(response);
    }

    private GameResponse mapToResponse(Game game) {
        Player player = game.getPlayer();
        Crupier crupier = game.getCrupier();

        PlayerResponse playerResponse = new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getHand().getCards().stream()
                        .map(this::mapToCardResponse)
                        .collect(Collectors.toList()),
                player.getScore(),
                player.getStatus()
        );

        List<CardResponse> crupierHand = crupier.getHand().getCards().stream()
                .map(this::mapToCardResponse)
                .collect(Collectors.toList());

        return new GameResponse(
                game.getId(),
                game.getGameStatus(),
                game.getGameResult(),
                playerResponse,
                crupierHand,
                crupier.getScore()
        );
    }

    private CardResponse mapToCardResponse(Card card) {
        String rank = capitalize(card.getRank().name().toLowerCase());
        String suit = capitalize(card.getSuit().name().toLowerCase());
        int value = card.getNumericValue();
        return new CardResponse(rank, suit,value);
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public Mono<Game> getGameById(UUID id) {
        return getGameById(id);
    }

    public Mono<Void> deleteById(UUID id) {
        getGameById(id).;
    }
}
