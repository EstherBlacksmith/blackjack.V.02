package com.itacademy.blackjack.game.infrastructure.persistence.mongo;


import com.itacademy.blackjack.config.TestMongoConfig;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MongoDB connectivity and GameDocument repository operations.
 *
 * This test verifies:
 * 1. MongoDB connection is working
 * 2. CRUD operations work correctly
 * 3. Custom query methods function as expected
 */
@DataMongoTest
@Import(TestMongoConfig.class)
class GameMongoRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private GameMongoRepository gameMongoRepository;

    private GameDocument sampleGame;

    @BeforeEach
    void setUp() {
        // Clear any existing data before each test
        gameMongoRepository.deleteAll().block();

        // Create a sample game document for testing
        GameDocument.CardDocument card1 = GameDocument.CardDocument.builder()
                .rank("ACE")
                .suit("SPADES")
                .value(11)
                .build();

        GameDocument.CardDocument card2 = GameDocument.CardDocument.builder()
                .rank("KING")
                .suit("HEARTS")
                .value(10)
                .build();

        sampleGame = GameDocument.builder()
                .playerName("TestPlayer")
                .playerCards(Arrays.asList(card1, card2))
                .playerScore(21)
                .crupierCards(Arrays.asList(
                        GameDocument.CardDocument.builder()
                                .rank("TEN")
                                .suit("DIAMONDS")
                                .value(10)
                                .build()
                ))
                .crupierScore(10)
                .gameStatus(GameStatus.FINISHED)
                .gameResult(GameResult.PLAYER_WINS)
                .createdAt(Instant.now())
                .finishedAt(Instant.now())
                .build();
    }

    @Test
    void testSaveAndFindById() {
        // Given: A game document to save

        // When: Save the document
        Mono<GameDocument> savedGame = gameMongoRepository.save(sampleGame);

        // Then: Verify save and find operations
        StepVerifier.create(savedGame)
                .assertNext(game -> {
                    assertNotNull(game.getId(), "Saved game should have an ID");
                    assertEquals("TestPlayer", game.getPlayerName(), "Player name should match");
                    assertEquals(21, game.getPlayerScore(), "Player score should be 21");
                    assertEquals(2, game.getPlayerCards().size(), "Player should have 2 cards");
                })
                .verifyComplete();
    }

    @Test
    void testFindByPlayerName() {
        // Given: A saved game document
        gameMongoRepository.save(sampleGame).block();

        // When: Find games by player name
        Flux<GameDocument> games = gameMongoRepository.findByPlayerName("TestPlayer");

        // Then: Verify results
        StepVerifier.create(games)
                .assertNext(game -> {
                    assertEquals("TestPlayer", game.getPlayerName());
                })
                .verifyComplete();
    }

    @Test
    void testFindByGameStatus() {
        // Given: A game with FINISHED status
        gameMongoRepository.save(sampleGame).block();

        // When: Find games by status
        Flux<GameDocument> finishedGames = gameMongoRepository.findByGameStatus("FINISHED");

        // Then: Verify we find the game
        StepVerifier.create(finishedGames)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testFindByGameResult() {
        // Given: A game where player won
        gameMongoRepository.save(sampleGame).block();

        // When: Find games by result
        Flux<GameDocument> wonGames = gameMongoRepository.findByGameResult("PLAYER_WINS");

        // Then: Verify we find the game
        StepVerifier.create(wonGames)
                .assertNext(game -> {
                    assertEquals("PLAYER_WINS", game.getGameResult().toString());
                })
                .verifyComplete();
    }

    @Test
    void testDeleteById() {
        // Given: A saved game
        GameDocument saved = gameMongoRepository.save(sampleGame).block();
        assertNotNull(saved.getId());

        // When: Delete the game
        Mono<Void> deleteResult = gameMongoRepository.deleteById(saved.getId());

        // Then: Verify deletion
        StepVerifier.create(deleteResult)
                .verifyComplete();

        // Verify the game is no longer findable
        StepVerifier.create(gameMongoRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testCount() {
        // Given: Multiple saved games
        gameMongoRepository.save(sampleGame).block();

        GameDocument anotherGame = GameDocument.builder()
                .playerName("AnotherPlayer")
                .playerScore(18)
                .gameStatus(GameStatus.FINISHED)
                .gameResult(GameResult.CRUPIER_WINS)
                .build();
        gameMongoRepository.save(anotherGame).block();

        // When: Count all games
        Mono<Long> count = gameMongoRepository.count();

        // Then: Verify count is correct
        StepVerifier.create(count)
                .expectNext(2L)
                .verifyComplete();
    }
}
