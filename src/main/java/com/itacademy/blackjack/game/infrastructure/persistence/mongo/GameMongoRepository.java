package com.itacademy.blackjack.game.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
/**
 * Repository for Game document persistence operations.
 * Provides reactive CRUD operations and custom queries.
 */
public interface GameMongoRepository extends ReactiveMongoRepository<GameDocument, String> {


    Flux<GameDocument> findByPlayerName(String playerName);

    Flux<GameDocument> findByGameStatus(String gameStatus);

    Flux<GameDocument> findByGameResult(String gameResult);
}
