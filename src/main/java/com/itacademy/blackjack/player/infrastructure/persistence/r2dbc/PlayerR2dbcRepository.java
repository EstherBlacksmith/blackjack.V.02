package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import com.itacademy.blackjack.player.domain.model.Player;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlayerR2dbcRepository extends ReactiveCrudRepository<PlayerEntity, String> {
    Mono<Player> findByName(String name);
}