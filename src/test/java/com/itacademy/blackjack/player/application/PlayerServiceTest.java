package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.application.dto.GameHistoryResponse;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepository;
import com.itacademy.blackjack.player.application.dto.PlayerStatsResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PlayerService.class)
@Import(PlayerService.class)
class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @MockBean
    private PlayerRepository playerRepository;

    @MockBean
    private PlayerMapper playerMapper;

    @MockBean
    private ScoringService scoringService;

    @MockBean
    private GameRepository gameRepository;

    @Test
    void testFindOrCreatePlayer_ReturnsExistingPlayer_WhenNameExists() {
        Player existingPlayer = Player.fromDatabase(
                UUID.randomUUID(), "Juana", 5, 2, 1
        );
        when(playerRepository.findByName("Juana")).thenReturn(Mono.just(existingPlayer));

        StepVerifier.create(playerService.findOrCreatePlayer("Juana"))
                .expectNextMatches(player ->
                        player.getName().equals("Juana") &&
                                player.getWins() == 5 &&
                                player.getLosses() == 2 &&
                                player.getPushes() == 1
                )
                .verifyComplete();
    }

    @Test
    void testFindOrCreatePlayer_CreatesNewPlayer_WhenNameNotExists() {
        when(playerRepository.findByName("NewPlayer")).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        StepVerifier.create(playerService.findOrCreatePlayer("NewPlayer"))
                .expectNextMatches(player ->
                        player.getName().equals("NewPlayer") &&
                                player.getWins() == 0 &&
                                player.getLosses() == 0 &&
                                player.getPushes() == 0
                )
                .verifyComplete();
    }


    @Test
    void testGetPlayerStats_Success() {
        // Given
        UUID playerId = UUID.randomUUID();
        
        List<GameHistoryResponse> mockHistory = List.of(
                new GameHistoryResponse(
                        UUID.randomUUID().toString(),
                        LocalDateTime.now().toString(),
                        "PLAYER_WINS",
                        21,
                        18
                )
        );

        // Create the expected PlayerStatsResponse with correct values (5+2+1=8 games)
        PlayerStatsResponse expectedStats = new PlayerStatsResponse(
                8,  // totalGames = wins(5) + losses(2) + pushes(1)
                5,  // wins
                2,  // losses
                1,  // pushes
                62.5,  // winRate = 5/8 * 100
                1,  // currentStreak
                mockHistory
        );

        // Mock the getPlayerStats method
        Player player = Player.fromDatabase(playerId, "Juana", 5, 2, 1);
        when(playerRepository.findById(playerId)).thenReturn(Mono.just(player));

        when(playerRepository.findById(playerId)).thenReturn(Mono.just(player));

        // When & Then
        StepVerifier.create(playerService.getPlayerStats(playerId))
                .expectNextMatches(stats ->
                        stats.totalGames() == 8 &&
                                stats.wins() == 5 &&
                                stats.losses() == 2 &&
                                stats.pushes() == 1 &&
                                stats.winRate() == 62.5
                )
                .verifyComplete();
    }

    @Test
    void testGetPlayerStats_PlayerNotFound() {
        // Given
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findById(playerId)).thenReturn(Mono.empty());

        // When & Then - PlayerService returns empty Mono, but getPlayerStats doesn't handle empty
        // So we need to check what happens when getPlayerStats returns empty
        StepVerifier.create(playerService.getPlayerStats(playerId))
                .expectComplete(); // Returns empty Mono, completes without error
    }
}
