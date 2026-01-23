package com.itacademy.blackjack.game.infrastructure.web;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.application.dto.CardResponse;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.application.dto.PlayerResponse;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.domain.model.PlayerStatus;
import com.itacademy.blackjack.game.domain.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(GameController.class)
@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GameService gameService;

    @MockBean
    private ScoringService scoringService;

    @MockBean
    private GameRepository gameRepository;

    private UUID testGameId;
    private UUID testPlayerId;
    private GameResponse testGameResponse;

    @BeforeEach
    void setUp() {
        testGameId = UUID.randomUUID();
        testPlayerId = UUID.randomUUID();

        testGameResponse = new GameResponse(
                testGameId,
                GameStatus.PLAYER_TURN,
                GameResult.NO_RESULTS_YET,
                new PlayerResponse(
                        testPlayerId,
                        "TestPlayer",
                        List.of(new CardResponse("Ace", "Spades", 11)),
                        21,
                        PlayerStatus.ACTIVE
                ),
                List.of(new CardResponse("Ten", "Hearts", 10)),
                10
        );
    }

    @Test
    void testStartNewGame_ReturnsCreatedGame() {
        // Given
        when(gameService.startNewGame(any(UUID.class))).thenReturn(Mono.just(testGameResponse));

        // When & Then
        webTestClient.post()
                .uri("/games/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"playerId\": \"" + testPlayerId + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(testGameId.toString())
                .jsonPath("$.status").isEqualTo("PLAYER_TURN")
                .jsonPath("$.player.id").isEqualTo(testPlayerId.toString())
                .jsonPath("$.player.name").isEqualTo("TestPlayer");
    }

    @Test
    void testGetGameById_ReturnsGame() {
        // Given
        when(gameService.getGameById(testGameId)).thenReturn(Mono.just(testGameResponse));

        // When & Then
        webTestClient.get()
                .uri("/games/{id}", testGameId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testGameId.toString())
                .jsonPath("$.status").isEqualTo("PLAYER_TURN");
    }

    @Test
    void testGetGameById_ReturnsNotFound_WhenGameDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(gameService.getGameById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/games/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testHit_ReturnsUpdatedGame() {
        // Given
        when(gameService.playerHit(testGameId)).thenReturn(Mono.just(testGameResponse));

        // When & Then
        webTestClient.post()
                .uri("/games/{id}/hit", testGameId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testGameId.toString())
                .jsonPath("$.status").isEqualTo("PLAYER_TURN");
    }

    @Test
    void testStand_ReturnsUpdatedGame() {
        // Given
        when(gameService.playerStand(testGameId)).thenReturn(Mono.just(testGameResponse));

        // When & Then
        webTestClient.post()
                .uri("/games/{id}/stand", testGameId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testGameId.toString());
    }

    @Test
    void testDeleteGame_ReturnsNoContent() {
        // Given
        when(gameService.deleteById(testGameId)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/games/{id}/delete", testGameId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
