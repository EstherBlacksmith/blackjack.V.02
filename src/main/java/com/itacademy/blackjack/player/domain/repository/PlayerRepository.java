package com.itacademy.blackjack.player.domain.repository;

import com.itacademy.blackjack.player.domain.model.Player;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface PlayerRepository {
    Mono<Player> findById(UUID id);
    Mono<Player> save(Player player);
    Mono<Void> deleteById(UUID id);
    Mono<Player> findByName(String name);

    Mono<Player> updateStats(UUID playerId, int wins, int losses, int pushes);

    Flux<Player> findAllByOrderByWinsDesc();
}
