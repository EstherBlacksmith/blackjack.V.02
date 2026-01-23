package com.itacademy.blackjack.player.domain.repository;

import com.itacademy.blackjack.player.domain.model.Player;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PlayerRepository extends ReactiveCrudRepository<Player, UUID> {
    Mono<Player> findByName(String name);
}