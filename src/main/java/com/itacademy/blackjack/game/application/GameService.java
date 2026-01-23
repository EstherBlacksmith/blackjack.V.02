package com.itacademy.blackjack.game.application;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;

import com.itacademy.blackjack.game.application.dto.CardResponse;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.application.dto.PlayerResponse;
import com.itacademy.blackjack.game.domain.model.Crupier;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.domain.model.Player;
import com.itacademy.blackjack.game.domain.model.exception.MissingIdentifierException;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import com.itacademy.blackjack.game.domain.repository.GameRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final ScoringService scoringService;
    private final GameRepository gameRepository;

    public GameService(ScoringService scoringService, GameRepository gameRepository) {
        this.scoringService = scoringService;
        this.gameRepository = gameRepository;
    }

    public Mono<GameResponse> startNewGame(UUID playerId) {
        Game game = new Game(scoringService);
        game.startGame();

        gameRepository.save(game);

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

    public Mono<GameResponse> getGameById(UUID gameId) {
        return Mono.justOrEmpty(gameRepository.findById(gameId))
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new ResourceNotFoundException("Game not found with id: " + gameId))));
    }

    public Mono<Void> deleteById(UUID id) {
        gameRepository.delete(id);
        return Mono.empty();
    }

    public Mono<GameResponse> playerHit(UUID gameId) {
        // Get the actual Game entity from repository
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));

        // Execute the hit logic
        game.playerHit();

        // Save the updated game state
        gameRepository.save(game);

        // Return the response with updated game state
        return Mono.just(mapToResponse(game));
    }


    public Mono<GameResponse> playerStand(UUID gameId) {
        // Get the actual Game entity from repository
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));

        // Execute the stand logic (dealer plays, winner determined)
        game.playerStand();

        // Save the updated game state
        gameRepository.save(game);

        // Return the response with game result
        return Mono.just(mapToResponse(game));
    }




}

