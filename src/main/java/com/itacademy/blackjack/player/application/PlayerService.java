package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
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

    public Mono<Player> createPlayer(String name) {
        Player player = Player.createNew(name, scoringService);
        return playerRepository.save(player);
    }

    public Mono<Player> findById(UUID playerId) {
        return playerRepository.findById(playerId);
    }
}
