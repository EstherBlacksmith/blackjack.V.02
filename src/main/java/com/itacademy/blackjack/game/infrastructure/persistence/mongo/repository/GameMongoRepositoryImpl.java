package com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository;

import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.domain.repository.GameRepository;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * MongoDB implementation of GameRepository.
 * Uses reactive Spring Data MongoDB for persistence operations.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GameMongoRepositoryImpl implements GameRepository {

    private final GameMongoRepository mongoRepository;
    private final GameMapper gameMapper;

    @Override
    public Mono<Game> save(Game game) {
        GameDocument document = gameMapper.toDocument(game);
        return mongoRepository.save(document)
                .map(gameMapper::toDomain) // Convert saved document back to domain
                .doOnSuccess(savedGame -> log.debug("Game saved with id: {}", savedGame.getId()))
                .doOnError(error -> log.error("Failed to save game: {}", error.getMessage()));

    }

    @Override
    public Mono<Game> findById(UUID id) {
        return mongoRepository.findById(id.toString())
                .map(gameMapper::toDomain)
                .doOnSuccess(game -> log.debug("Found game with id: {}", id))
                .doOnError(error -> log.error("Failed to find game with id {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return mongoRepository.deleteById(id.toString())
                .doOnSuccess(v -> log.debug("Deleted game with id: {}", id))
                .doOnError(error -> log.error("Failed to delete game with id {}: {}", id, error.getMessage()));
    }
}
