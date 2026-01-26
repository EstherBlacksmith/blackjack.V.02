package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ScoringService scoringService;

    public PlayerService(PlayerRepository playerRepository,
                         PlayerMapper playerMapper,
                         ScoringService scoringService) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.scoringService = scoringService;
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
}
