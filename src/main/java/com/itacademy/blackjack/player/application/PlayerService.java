package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.player.application.dto.PlayerRankingResponse;
import com.itacademy.blackjack.player.application.dto.PlayerStatsResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.itacademy.blackjack.game.application.dto.GameHistoryResponse;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final GameRepository gameRepository;

    public PlayerService(PlayerRepository playerRepository, PlayerMapper playerMapper, GameRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.gameRepository = gameRepository;
    }

    public Mono<Player> findOrCreatePlayer(String name) {
        log.debug("findOrCreatePlayer called with name: {}", name);
        return playerRepository.findByName(name)
                .switchIfEmpty(
                        Mono.defer(() -> createPlayer(name))
                );
    }


    public Mono<Player> createPlayer(String name) {
        log.info("Creating new player with name: {}", name);
        Player player = Player.createNew(name);
        return playerRepository.save(player);
    }

    public Mono<Player> findById(UUID playerId) {
        log.debug("findById called for playerId: {}", playerId);
        return playerRepository.findById(playerId);
    }

    public Mono<Player> findByName(String name) {
        log.debug("findByName called for name: {}", name);
        return playerRepository.findByName(name);
    }

    public Mono<Player> updatePlayerStats(UUID playerId, GameResult result) {
        log.debug("updatePlayerStats called for playerId: {}, result: {}", playerId, result);
        return playerRepository.findById(playerId)
                .flatMap(player -> {
                    player.applyGameResult(result);
                    return playerRepository.updateStats(playerId, player.getWins(), player.getLosses(), player.getPushes());
                });
    }

    public Mono<Void> deleteById(UUID playerId) {
        log.info("Deleting player with id: {}", playerId);
        return playerRepository.deleteById(playerId);
    }

    public Flux<GameHistoryResponse> getPlayerGameHistory(UUID playerId) {
        log.debug("getPlayerGameHistory called for playerId: {}", playerId);
        return gameRepository.findDocumentsByPlayerId(playerId)
                .filter(game -> game.getGameResult() != null &&
                        game.getGameResult() != GameResult.NO_RESULTS_YET)
                .map(game -> {
                    // Format the date from Instant
                    Instant dateInstant = game.getFinishedAt() != null ? game.getFinishedAt() : game.getCreatedAt();
                    String dateStr = "N/A";
                    if (dateInstant != null) {
                        try {
                            dateStr = dateInstant.atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
                        } catch (Exception e) {
                            dateStr = "Recent";
                        }
                    }
                    return new GameHistoryResponse(
                            game.getId(),
                            dateStr,
                            game.getGameResult().name(),
                            game.getPlayerScore(),
                            game.getCrupierScore()
                    );
                });
    }

    public Mono<PlayerStatsResponse> getPlayerStats(UUID playerId) {
        log.debug("getPlayerStats called for playerId: {}", playerId);
        return findById(playerId)
                .zipWith(getPlayerGameHistory(playerId).collectList())
                .map(tuple -> {
                    Player player = tuple.getT1();
                    List<GameHistoryResponse> recentGames = tuple.getT2();
                    var stats = player.getStats();
                    return new PlayerStatsResponse(
                            stats.totalGames(),
                            stats.wins(),
                            stats.losses(),
                            stats.pushes(),
                            stats.winRate(),
                            0, // streak
                            recentGames
                    );
                });
    }

    public Mono<Void> updateStatsOnly(UUID playerId, int wins, int losses, int pushes) {
        log.debug("updateStatsOnly called for playerId: {}, wins: {}, losses: {}, pushes: {}",
                playerId, wins, losses, pushes);
        return playerRepository.updateStats(playerId, wins, losses, pushes).then();
    }

    public Flux<PlayerRankingResponse> getPlayerRanking() {
        log.debug("getPlayerRanking called");
        return playerRepository.findAllByOrderByWinsDesc()
                .index() // Returns Tuple2<Long, Player> where T1 is index
                .map(tuple -> {
                    long rank = tuple.getT1() + 1; // Convert 0-based index to 1-based rank
                    Player player = tuple.getT2();
                    return new PlayerRankingResponse(
                            (int) rank,
                            player.getId().toString(),
                            player.getName(),
                            player.getWins(),
                            player.getLosses(),
                            player.getPushes()
                    );
                });
    }

}
