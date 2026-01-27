package com.itacademy.blackjack.player.infrastructure.web;

import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.application.dto.CreatePlayerRequest;
import com.itacademy.blackjack.player.application.dto.PlayerProfileResponse;
import com.itacademy.blackjack.player.application.dto.PlayerRankingResponse;
import com.itacademy.blackjack.player.application.dto.PlayerStatsResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/players")
@Tag(name = "Player Management", description = "APIs for managing Blackjack players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/new")
    @Operation(summary = "Create new player", description = "Creates a new player with the specified name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player created successfully",
                    content = @Content(schema = @Schema(implementation = PlayerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PlayerProfileResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return playerService.createPlayer(request.name())
                .map(this::toProfileResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login or create player", description = "Returns existing player or creates a new one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found or created",
                    content = @Content(schema = @Schema(implementation = PlayerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<PlayerProfileResponse> loginOrCreate(@Valid @RequestBody CreatePlayerRequest request) {
        return playerService.findOrCreatePlayer(request.name())
                .map(this::toProfileResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", description = "Retrieves a specific player by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found",
                    content = @Content(schema = @Schema(implementation = PlayerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<PlayerProfileResponse> getPlayerById(@PathVariable UUID id) {
        return playerService.findById(id)
                .map(this::toProfileResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Player not found with id: " + id)));
    }

    private PlayerProfileResponse toProfileResponse(Player player) {
        return new PlayerProfileResponse(
                player.getId(),
                player.getName(),
                player.getStatus(),
                player.getWins(),
                player.getLosses(),
                player.getPushes()
        );
    }

    @GetMapping("/{playerId}/stats")
    @Operation(summary = "Get player statistics", description = "Retrieves detailed statistics for a specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PlayerStatsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<PlayerStatsResponse> getPlayerStats(@PathVariable UUID playerId) {
        return playerService.getPlayerStats(playerId);
    }

    @GetMapping("/ranking")
    @Operation(summary = "Get player rankings", description = "Retrieves all players sorted by their win rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PlayerRankingResponse.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    public Flux<PlayerRankingResponse> getPlayerRanking() {
        return playerService.getPlayerRanking();
    }
}
