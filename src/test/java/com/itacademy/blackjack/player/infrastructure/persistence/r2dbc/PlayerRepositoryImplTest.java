package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

// Use integration test approach with Testcontainers
@SpringBootTest
@Testcontainers
class PlayerRepositoryImplTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void save_player_savesSuccessfully() {
        // Given
        UUID playerId = UUID.randomUUID();
        Player player = Player.fromDatabase(playerId, "TestPlayer", 0, 0, 0);

        // When & Then
        StepVerifier.create(playerRepository.save(player))
                .expectNextMatches(p -> p.getName().equals("TestPlayer"))
                .verifyComplete();
    }

    @Test
    void findById_existingPlayer_returnsPlayer() {
        // Given
        UUID playerId = UUID.randomUUID();
        Player player = Player.fromDatabase(playerId, "FindTest", 5, 2, 1);
        Mono<Player> saved = playerRepository.save(player);

        // When & Then
        StepVerifier.create(saved.then(playerRepository.findById(playerId)))
                .expectNextMatches(p -> p.getId().equals(playerId))
                .verifyComplete();
    }
}
