package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerEntity;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerR2dbcRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerR2dbcRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ScoringService scoringService;

    public PlayerService(PlayerR2dbcRepository playerRepository,
                         PlayerMapper playerMapper,
                         ScoringService scoringService) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.scoringService = scoringService;
    }

    public Mono<Player> createPlayer(String name) {
        Player player = Player.createNew(name, scoringService);
        PlayerEntity entity = playerMapper.toEntity(player);
        return playerRepository.save(entity)
                .thenReturn(player);
    }

    public Mono<Player> findById(UUID playerId) {
        return playerRepository.findById(playerId.toString())
                .map(playerMapper::toDomain);
    }

    public Mono<Player> updateStats(Player player) {
        PlayerEntity entity = playerMapper.toEntity(player);
        return playerRepository.save(entity)
                .thenReturn(player);
    }
}
