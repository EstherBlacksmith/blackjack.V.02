package com.itacademy.blackjack.game.application;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.application.dto.CardResponse;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.application.dto.PlayerResponse;
import com.itacademy.blackjack.game.domain.model.Crupier;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepository;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.application.PlayerService;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final PlayerService playerService;
    private final ScoringService scoringService;
    private final GameRepository gameRepository;

    public GameService(PlayerService playerService, ScoringService scoringService, GameRepository gameRepository) {
        this.playerService = playerService;
        this.scoringService = scoringService;
        this.gameRepository = gameRepository;
    }

    public Mono<GameResponse> startNewGame(UUID playerId) {
        return playerService.findById(playerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Player not found: " + playerId)))
                .flatMap(player -> {
                    Game game = new Game(scoringService);
                    game.setPlayer(player);
                    game.startGame();
                    return gameRepository.save(game).map(this::mapToResponse);
                });
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
        return new CardResponse(rank, suit, value);
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public Mono<GameResponse> getGameById(UUID gameId) {
        return gameRepository.findById(gameId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Game not found with id: " + gameId)));
    }

    public Mono<Void> deleteById(UUID id) {
        return gameRepository.deleteById(id);
    }

    public Mono<GameResponse> playerHit(UUID gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Game not found with id: " + gameId)))
                .flatMap(game -> {
                    game.playerHit();
                    return gameRepository.save(game)
                            .flatMap(savedGame -> {
                                if (savedGame.getGameResult() != null &&
                                        savedGame.getGameResult() != GameResult.NO_RESULTS_YET) {
                                    return playerService.updatePlayerStats(
                                            savedGame.getPlayer().getId(),
                                            savedGame.getGameResult()
                                    ).thenReturn(savedGame);
                                }
                                return Mono.just(savedGame);
                            });
                })
                .map(this::mapToResponse);
    }


    public Mono<GameResponse> playerStand(UUID gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Game not found with id: " + gameId)))
                .flatMap(game -> {
                    game.playerStand();
                    return gameRepository.save(game)
                            .flatMap(savedGame -> {
                                // Update player stats
                                return playerService.updatePlayerStats(
                                        savedGame.getPlayer().getId(),
                                        savedGame.getGameResult()
                                ).thenReturn(savedGame);
                            });
                })
                .map(this::mapToResponse);
    }

}

