package com.itacademy.blackjack.player.application.dto;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerMapper;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerRankingTest {

    @Mock
    PlayerRepository playerRepository;

    @Mock
    PlayerMapper playerMapper;

    @Mock
    ScoringService scoringService;

    @Mock
    GameRepository gameRepository;

    @Test
    void shouldReturnPlayersOrderedByWinsDesc() {
        // Given - Create real players
        Player player1 = Player.createNew("Alice", scoringService);
        player1.applyGameResult(GameResult.PLAYER_WINS);
        player1.applyGameResult(GameResult.PLAYER_WINS);

        Player player2 = Player.createNew("Bob", scoringService);
        player2.applyGameResult(GameResult.PLAYER_WINS);

        // Create the real service with mocked dependencies
        PlayerService playerService = new PlayerService(
                playerRepository,
                playerMapper,
                scoringService,
                gameRepository
        );

        // Mock the repository method
        when(playerRepository.findAllByOrderByWinsDesc())
                .thenReturn(Flux.just(player1, player2));

        // When & Then
        StepVerifier.create(playerService.getPlayerRanking())
                .expectNextMatches(ranking ->
                        ranking.rank() == 1 &&
                                ranking.playerName().equals("Alice") &&
                                ranking.wins() == 2)
                .expectNextMatches(ranking ->
                        ranking.rank() == 2 &&
                                ranking.playerName().equals("Bob") &&
                                ranking.wins() == 1)
                .verifyComplete();
    }
}
