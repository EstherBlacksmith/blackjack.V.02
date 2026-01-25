package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import com.itacademy.blackjack.player.domain.model.Player;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlayerR2dbcRepository extends ReactiveCrudRepository<PlayerEntity, String> {
    @Query("INSERT INTO players (id, name, wins, losses, pushes) VALUES (:id, :name, :wins, :losses, :pushes)")
    Mono<Void> insert(String id, String name, int wins, int losses, int pushes);

    Mono<Player> findByName(String name);
}