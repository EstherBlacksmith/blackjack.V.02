package com.itacademy.blackjack.game.infrastructure.web;

import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.application.dto.GameRequest;
import com.itacademy.blackjack.game.application.dto.GameResponse;
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
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/games")
@Tag(name = "Game Management", description = "APIs for managing Blackjack games")
public class GameController {


    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    @Operation(summary = "Start a new game", description = "Creates and starts a new Blackjack game for the specified player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game created successfully",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid player ID")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameResponse> startNewGame(@Valid @RequestBody GameRequest gameRequest) {
        return gameService.startNewGame(gameRequest.playerId());
    }

    @GetMapping("{id}")
    @Operation(summary = "Get game by ID", description = "Retrieves a specific Blackjack game by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> getGameById(@PathVariable UUID id) throws ResourceNotFoundException {

        return gameService.getGameById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Game not found with id: " + id)));
    }

    @PostMapping("/{id}/hit")
    @Operation(summary = "Player hits", description = "Player draws a card from the deck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card drawn successfully",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found"),
            @ApiResponse(responseCode = "400", description = "Not player's turn or game over")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> hit(@PathVariable UUID id) {
        return gameService.playerHit(id);
    }

    @PostMapping("/{id}/stand")
    @Operation(summary = "Player stands", description = "Player ends their turn, crupier plays next")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turn ended successfully",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> stand(@PathVariable UUID id) {
        return gameService.playerStand(id);
    }

    @PostMapping("/{gameId}/crupier-hit")
    @Operation(summary = "Crupier draws card", description = "Crupier draws one card (used after player stands)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crupier drew card",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> crupierHit(@PathVariable UUID gameId) {
        return gameService.crupierHitOneCard(gameId);
    }

    @DeleteMapping("{id}/delete")
    @Operation(summary = "Delete game", description = "Deletes a Blackjack game by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Game deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable UUID id) throws ResourceNotFoundException {
        return gameService.deleteById(id);
    }

}
