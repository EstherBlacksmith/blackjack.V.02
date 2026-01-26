package com.itacademy.blackjack.player.application;

import com.itacademy.blackjack.deck.model.ScoringService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PlayerService.class)
@Import(PlayerService.class)  // ← AÑADIR ESTO
class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @MockBean
    private PlayerRepository playerRepository;

    @MockBean
    private PlayerMapper playerMapper;

    @MockBean
    private ScoringService scoringService;

    @Test
    void testFindOrCreatePlayer_ReturnsExistingPlayer_WhenNameExists() {
        Player existingPlayer = Player.fromDatabase(
                UUID.randomUUID(), "John", 5, 2, 1
        );
        when(playerRepository.findByName("John")).thenReturn(Mono.just(existingPlayer));

        StepVerifier.create(playerService.findOrCreatePlayer("John"))
                .expectNextMatches(player ->
                        player.getName().equals("John") &&
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
}
