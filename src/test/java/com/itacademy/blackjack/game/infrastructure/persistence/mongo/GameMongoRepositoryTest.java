package com.itacademy.blackjack.game.infrastructure.persistence.mongo;

import com.itacademy.blackjack.config.TestMongoConfig;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for MongoDB connectivity and GameDocument repository operations.
 */
@DataMongoTest
@Import(TestMongoConfig.class)
class GameMongoRepositoryTest {


    @Autowired
    private GameMongoRepository gameMongoRepository;

    private GameDocument sampleGame;

    @BeforeEach
    void setUp() {
        gameMongoRepository.deleteAll().block();
        sampleGame = createSampleGame("TestPlayer", GameStatus.FINISHED, GameResult.PLAYER_WINS);
    }

    private GameDocument createSampleGame(String playerName, GameStatus status, GameResult result) {
        GameDocument.CardDocument card1 = new GameDocument.CardDocument("ACE", "SPADES", 11);
        GameDocument.CardDocument card2 = new GameDocument.CardDocument("KING", "HEARTS", 10);

        return GameDocument.builder()
                .playerName(playerName)
                .playerCards(Arrays.asList(card1, card2))
                .playerScore(21)
                .crupierCards(Arrays.asList(new GameDocument.CardDocument("TEN", "DIAMONDS", 10)))
                .crupierScore(10)
                .gameStatus(status)
                .gameResult(result)
                .createdAt(Instant.now())
                .finishedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Happy Path Tests - CRUD Operations")
    class HappyPathTests {

        @Test
        @DisplayName("Save and find game by ID")
        void testSaveAndFindById() {
            StepVerifier.create(gameMongoRepository.save(sampleGame))
                    .assertNext(game -> {
                        assertNotNull(game.getId(), "Saved game should have an ID");
                        assertEquals("TestPlayer", game.getPlayerName(), "Player name should match");
                        assertEquals(21, game.getPlayerScore(), "Player score should be 21");
                        assertEquals(2, game.getPlayerCards().size(), "Player should have 2 cards");
                    })
                    .verifyComplete();

            StepVerifier.create(gameMongoRepository.findById(sampleGame.getId()))
                    .assertNext(game -> assertEquals(sampleGame.getPlayerName(), game.getPlayerName()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Find games by player name")
        void testFindByPlayerName() {
            gameMongoRepository.save(sampleGame).block();

            StepVerifier.create(gameMongoRepository.findByPlayerName("TestPlayer"))
                    .assertNext(game -> assertEquals("TestPlayer", game.getPlayerName()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Find games by game status")
        void testFindByGameStatus() {
            gameMongoRepository.save(sampleGame).block();

            StepVerifier.create(gameMongoRepository.findByGameStatus("FINISHED"))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Find games by game result")
        void testFindByGameResult() {
            gameMongoRepository.save(sampleGame).block();

            // FIXED: Compare enums directly instead of converting to String
            StepVerifier.create(gameMongoRepository.findByGameResult("PLAYER_WINS"))
                    .assertNext(game -> assertEquals(GameResult.PLAYER_WINS, game.getGameResult()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Update game and verify persistence")
        void testUpdateGame() {
            GameDocument saved = gameMongoRepository.save(sampleGame).block();
            assertNotNull(saved.getId());

            saved.setGameStatus(GameStatus.FINISHED);
            saved.setGameResult(GameResult.PLAYER_WINS);

            StepVerifier.create(gameMongoRepository.save(saved))
                    .assertNext(game -> {
                        assertEquals(GameStatus.FINISHED, game.getGameStatus());
                        assertEquals(GameResult.PLAYER_WINS, game.getGameResult());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Count all games")
        void testCount() {
            gameMongoRepository.save(sampleGame).block();
            GameDocument anotherGame = createSampleGame("AnotherPlayer", GameStatus.FINISHED, GameResult.CRUPIER_WINS);
            gameMongoRepository.save(anotherGame).block();

            StepVerifier.create(gameMongoRepository.count())
                    .expectNext(2L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Find all games returns all saved games")
        void testFindAllGames() {
            gameMongoRepository.save(sampleGame).block();
            GameDocument anotherGame = createSampleGame("AnotherPlayer", GameStatus.FINISHED, GameResult.CRUPIER_WINS);
            gameMongoRepository.save(anotherGame).block();

            StepVerifier.create(gameMongoRepository.findAll())
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Sad Path Tests - Edge Cases & Error Handling")
    class SadPathTests {

        @Test
        @DisplayName("Find non-existent game returns empty")
        void testFindNonExistentGame() {
            StepVerifier.create(gameMongoRepository.findById("non-existent-id"))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Delete non-existent game should not throw")
        void testDeleteNonExistentGame() {
            StepVerifier.create(gameMongoRepository.deleteById("non-existent-id"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Find by non-existent player name returns empty")
        void testFindByNonExistentPlayerName() {
            StepVerifier.create(gameMongoRepository.findByPlayerName("NonExistent"))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Delete game successfully")
        void testDeleteGame() {
            GameDocument saved = gameMongoRepository.save(sampleGame).block();
            assertNotNull(saved.getId());

            StepVerifier.create(gameMongoRepository.deleteById(saved.getId()))
                    .verifyComplete();

            StepVerifier.create(gameMongoRepository.findById(saved.getId()))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Complex game with multiple cards saves correctly")
        void testComplexGameWithMultipleCards() {
            GameDocument.CardDocument card1 = new GameDocument.CardDocument("ACE", "SPADES", 11);
            GameDocument.CardDocument card2 = new GameDocument.CardDocument("KING", "HEARTS", 10);
            GameDocument.CardDocument card3 = new GameDocument.CardDocument("THREE", "CLUBS", 3);
            GameDocument.CardDocument card4 = new GameDocument.CardDocument("QUEEN", "DIAMONDS", 10);
            GameDocument.CardDocument card5 = new GameDocument.CardDocument("SEVEN", "HEARTS", 7);

            GameDocument complexGame = GameDocument.builder()
                    .playerName("ComplexPlayer")
                    .playerCards(Arrays.asList(card1, card2, card3))
                    .playerScore(24)
                    .crupierCards(Arrays.asList(card4, card5))
                    .crupierScore(17)
                    .gameStatus(GameStatus.FINISHED)
                    .gameResult(GameResult.CRUPIER_WINS)
                    .createdAt(Instant.now())
                    .finishedAt(Instant.now())
                    .build();

            StepVerifier.create(gameMongoRepository.save(complexGame))
                    .assertNext(game -> {
                        assertEquals(3, game.getPlayerCards().size());
                        assertEquals(2, game.getCrupierCards().size());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Multiple consecutive saves work correctly")
        void testMultipleConsecutiveSaves() {
            Mono<GameDocument> save1 = gameMongoRepository.save(
                    createSampleGame("P1", GameStatus.FINISHED, GameResult.PLAYER_WINS));
            Mono<GameDocument> save2 = gameMongoRepository.save(
                    createSampleGame("P2", GameStatus.FINISHED, GameResult.CRUPIER_WINS));

            // FIX: Use Mono.when() to wait for both without emitting values
            StepVerifier.create(Mono.when(save1, save2))
                    .verifyComplete();

            StepVerifier.create(gameMongoRepository.count())
                    .expectNext(2L)
                    .verifyComplete();
        }
    }
}
