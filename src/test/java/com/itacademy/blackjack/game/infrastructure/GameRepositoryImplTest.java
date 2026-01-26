package com.itacademy.blackjack.game.infrastructure;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper.GameMapper;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameMongoRepository;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepositoryImpl;
import com.itacademy.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GameRepositoryImplTest {

    @Mock
    private GameMongoRepository mongoRepository;

    @Mock
    private GameMapper gameMapper;

    private GameRepositoryImpl gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new GameRepositoryImpl(mongoRepository, gameMapper);
    }

    @Test
    void save_whenValidGame_savesSuccessfully() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Player player = Player.fromDatabase(playerId, "TestPlayer", 0, 0, 0);
        ScoringService scoringService = new ScoringService();

        Game game = Game.builder()
                .id(gameId)
                .scoringService(scoringService)
                .player(player)
                .build();

        GameDocument document = new GameDocument();
        document.setId(gameId.toString());

        GameDocument savedDocument = new GameDocument();
        savedDocument.setId(gameId.toString());

        when(gameMapper.toDocument(game)).thenReturn(document);
        when(mongoRepository.save(any(GameDocument.class))).thenReturn(Mono.just(savedDocument));
        when(gameMapper.toDomain(savedDocument)).thenReturn(game);

        // When & Then
        StepVerifier.create(gameRepository.save(game))
                .expectNext(game)
                .verifyComplete();

        verify(mongoRepository).save(document);
    }

    @Test
    void findById_whenGameExists_returnsGame() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Game expectedGame = Game.builder()
                .id(gameId)
                .scoringService(new ScoringService())
                .player(Player.fromDatabase(playerId, "TestPlayer", 0, 0, 0))
                .build();

        GameDocument document = new GameDocument();
        document.setId(gameId.toString());

        when(mongoRepository.findById(gameId.toString())).thenReturn(Mono.just(document));
        when(gameMapper.toDomain(document)).thenReturn(expectedGame);

        // When & Then
        StepVerifier.create(gameRepository.findById(gameId))
                .expectNext(expectedGame)
                .verifyComplete();
    }

    @Test
    void findById_whenGameNotExists_returnsEmpty() {
        // Given
        UUID gameId = UUID.randomUUID();

        when(mongoRepository.findById(gameId.toString())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(gameRepository.findById(gameId))
                .verifyComplete();
    }

    @Test
    void deleteById_deletesSuccessfully() {
        // Given
        UUID gameId = UUID.randomUUID();

        when(mongoRepository.deleteById(gameId.toString())).thenReturn(Mono.empty().then());

        // When & Then
        StepVerifier.create(gameRepository.deleteById(gameId))
                .verifyComplete();

        verify(mongoRepository).deleteById(gameId.toString());
    }

    @Test
    void findByPlayerId_whenGamesExist_returnsGames() {
        // Given
        UUID playerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        Game game = Game.builder()
                .id(gameId)
                .scoringService(new ScoringService())
                .player(Player.fromDatabase(playerId, "TestPlayer", 0, 0, 0))
                .build();

        GameDocument document = new GameDocument();
        document.setId(gameId.toString());

        when(mongoRepository.findByPlayerId(playerId.toString()))
                .thenReturn(Flux.just(document));
        when(gameMapper.toDomain(document)).thenReturn(game);

        // When & Then
        StepVerifier.create(gameRepository.findByPlayerId(playerId))
                .expectNext(game)
                .verifyComplete();
    }

    @Test
    void findByPlayerId_whenNoGames_returnsEmpty() {
        // Given
        UUID playerId = UUID.randomUUID();

        when(mongoRepository.findByPlayerId(playerId.toString())).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(gameRepository.findByPlayerId(playerId))
                .verifyComplete();
    }
}
