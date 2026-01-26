package com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository;

import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final GameMongoRepository mongoRepository;
    private final GameMapper mapper;

    @Override
    public Mono<Game> save(Game game) {
        GameDocument document = mapper.toDocument(game);
        // Add timestamp if not already set
        if (document.getCreatedAt() == null) {
            document.setCreatedAt(Instant.now());
        }
        document.setFinishedAt(Instant.now());
        return mongoRepository.save(document)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Game> findById(UUID id) {
        return mongoRepository.findById(id.toString())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return mongoRepository.deleteById(id.toString());
    }

    @Override
    public Flux<Game> findByPlayerId(UUID playerId) {
        return mongoRepository.findByPlayerId(playerId.toString())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<GameDocument> findDocumentsByPlayerId(UUID playerId) {
        return mongoRepository.findByPlayerId(playerId.toString());
    }


}
