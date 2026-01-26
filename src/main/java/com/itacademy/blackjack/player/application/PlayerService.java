package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.player.application.dto.PlayerStatsResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.itacademy.blackjack.game.application.dto.GameHistoryResponse;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepository;

import java.util.List;
import java.util.UUID;
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ScoringService scoringService;
    private final GameRepository gameRepository;

    public PlayerService(PlayerRepository playerRepository,
                         PlayerMapper playerMapper,
                         ScoringService scoringService, GameRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.scoringService = scoringService;
        this.gameRepository = gameRepository;
    }

    public Mono<Player> findOrCreatePlayer(String name) {
        return playerRepository.findByName(name)
                .switchIfEmpty(
                        Mono.defer(() -> createPlayer(name))
                );
    }


    public Mono<Player> createPlayer(String name) {
        Player player = Player.createNew(name, scoringService);
        return playerRepository.save(player);
    }

    public Mono<Player> findById(UUID playerId) {
        return playerRepository.findById(playerId);
    }

    public Mono<Player> findByName(String name) {
        return playerRepository.findByName(name);
    }

    public Mono<Player> updatePlayerStats(UUID playerId, GameResult result) {
        return playerRepository.findById(playerId)
                .flatMap(player -> {
                    player.applyGameResult(result);
                    return playerRepository.updateStats(playerId, player.getWins(), player.getLosses(), player.getPushes());
                });
    }

    public Mono<Void> deleteById(UUID playerId) {
        return playerRepository.deleteById(playerId);
    }

    public Flux<GameHistoryResponse> getPlayerGameHistory(UUID playerId) {
        return gameRepository.findByPlayerId(playerId)
                .filter(game -> game.getGameResult() != null &&
                        game.getGameResult() != GameResult.NO_RESULTS_YET)
                .map(game -> new GameHistoryResponse(
                        game.getId().toString(),
                        "N/A",  // Game domain model has no timestamp
                        game.getGameResult().name(),
                        game.getPlayer().getScore(),
                        game.getCrupier().getScore()
                ));
    }

    public Mono<PlayerStatsResponse> getPlayerStats(UUID playerId) {
        return findById(playerId)
                .map(Player::getStats)
                .map(stats -> new PlayerStatsResponse(
                        stats.totalGames(),
                        stats.wins(),
                        stats.losses(),
                        stats.pushes(),
                        stats.winRate(),
                        0, // streak
                        List.of() // recent games
                ));
    }


}
