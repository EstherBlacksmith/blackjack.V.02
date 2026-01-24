package com.itacademy.blackjack.game.domain.repository;

import com.itacademy.blackjack.game.domain.model.Game;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GameRepository {
    Mono<Game> save(Game game);
    Mono<Game> findById(UUID id);
    Mono<Void> delete(UUID id);
}