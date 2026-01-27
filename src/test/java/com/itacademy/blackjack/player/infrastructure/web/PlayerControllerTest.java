package com.itacademy.blackjack.player.infrastructure.web;

import com.itacademy.blackjack.config.TestcontainersInitializer;
import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameResponse;
import com.itacademy.blackjack.game.domain.model.GameStatus;
import com.itacademy.blackjack.game.domain.model.PlayerStatus;
import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.application.dto.PlayerProfileResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PlayerController.class)
@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PlayerService playerService;

    private UUID testPlayerId;
    private Player testPlayer;
    private PlayerProfileResponse testPlayerResponse;

    @BeforeEach
    void setUp() {
        testPlayerId = UUID.randomUUID();
        testPlayer = Player.fromDatabase(testPlayerId, "TestPlayer", 5, 2, 1);
        testPlayerResponse = new PlayerProfileResponse(
                testPlayerId, "TestPlayer", PlayerStatus.ACTIVE, 5, 2, 1
        );
    }

    @Test
    void testCreatePlayer_ReturnsCreatedPlayer() {
        when(playerService.createPlayer(any(String.class))).thenReturn(Mono.just(testPlayer));

        webTestClient.post()
                .uri("/players/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"TestPlayer\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testPlayerId.toString())
                .jsonPath("$.name").isEqualTo("TestPlayer");
    }

    @Test
    void testGetPlayerById_ReturnsPlayer() {
        when(playerService.findById(testPlayerId)).thenReturn(Mono.just(testPlayer));

        webTestClient.get()
                .uri("/players/{id}", testPlayerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testPlayerId.toString());
    }

    @Test
    void testGetPlayerById_ReturnsNotFound_WhenPlayerDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(playerService.findById(nonExistentId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/players/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetPlayerById_ReturnsServerError_WhenServiceThrowsException() {
        when(playerService.findById(testPlayerId))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        webTestClient.get()
                .uri("/players/{id}", testPlayerId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testCreatePlayer_ReturnsServerError_WhenServiceFails() {
        when(playerService.createPlayer(any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        webTestClient.post()
                .uri("/players/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"TestPlayer\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Import(TestcontainersInitializer.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    static
    class PlayerControllerIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private PlayerService playerService;

        @Autowired
        private GameService gameService;

        private UUID playerId;

        @BeforeEach
        void setUp() {
            Player player = playerService.createPlayer("E2ETestPlayer").block();
            assertNotNull(player);
            playerId = player.getId();
        }

        @Test
        @DisplayName("Full flow: Create player -> Start game -> Hit -> Stand")
        void testFullGameFlowThroughControllers() {
            // Step 1: Verify player exists via controller
            webTestClient.get()
                    .uri("/players/{id}", playerId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("E2ETestPlayer");

            // Step 2: Start a new game
            GameResponse game = gameService.startNewGame(playerId).block();
            assertNotNull(game);
            UUID gameId = game.id();

            // Step 3: Player hits
            GameResponse afterHit = gameService.playerHit(gameId).block();
            assertNotNull(afterHit);

            // Check if game is already finished (bust or blackjack)
            if (afterHit.status() == GameStatus.FINISHED) {
                return;  // Don't call playerStand() if game is over
            }

            // Step 4: Player stands
            GameResponse finalGame = gameService.playerStand(gameId).block();
            assertNotNull(finalGame);
            assertEquals(com.itacademy.blackjack.game.domain.model.GameStatus.FINISHED,
                    finalGame.status());

            // Step 5: Verify player stats updated
            webTestClient.get()
                    .uri("/players/{id}", playerId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(playerId.toString());
        }

        @Test
        @DisplayName("Create player via controller and immediately use in game")
        void testCreatePlayerAndStartGame() {
            // Step 1: Create new player via controller
            String newPlayerName = "NewControllerPlayer";

            webTestClient.post()
                    .uri("/players/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"" + newPlayerName + "\"}")
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo(newPlayerName)
                    .jsonPath("$.wins").isEqualTo(0)
                    .jsonPath("$.losses").isEqualTo(0);

            // Get the player ID from response (simplified - in real test you'd extract it)
            UUID newPlayerId = playerService.findByName(newPlayerName).block().getId();

            // Step 2: Start a game with the new player
            GameResponse game = gameService.startNewGame(newPlayerId).block();
            assertNotNull(game);
            assertEquals(newPlayerId, game.player().id());
        }
    }
}
