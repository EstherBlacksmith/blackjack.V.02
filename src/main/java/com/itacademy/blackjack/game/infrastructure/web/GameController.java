package com.itacademy.blackjack.game.infrastructure.web;

import com.itacademy.blackjack.game.application.dto.GameRequest;
import com.itacademy.blackjack.game.application.dto.GameResponse;

import com.itacademy.blackjack.game.application.GameService;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {


    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameResponse> startNewGame(@Valid @RequestBody GameRequest gameRequest) {
        return gameService.startNewGame(gameRequest.playerId());
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> getGameById(@PathVariable UUID id) throws ResourceNotFoundException {

        return gameService.getGameById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Game not found with id: " + id)));
    }

    @PostMapping("/{id}/hit")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> hit(@PathVariable UUID id) {
        return gameService.playerHit(id);
    }

    @PostMapping("/{id}/stand")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameResponse> stand(@PathVariable UUID id) {
        return gameService.playerStand(id);
    }

/*    @PostMapping("{id}/play")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Game> makeMove( @PathVariable UUID id, @Valid @RequestBody MoveRequest moveRequest) throws MissingIdentifierException {

        return bettingService.makeMove(id, moveRequest);
    }*/

    @DeleteMapping("{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable UUID id) throws ResourceNotFoundException {
        return gameService.deleteById(id);
    }

}
