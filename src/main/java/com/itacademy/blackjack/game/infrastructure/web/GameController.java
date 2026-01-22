package com.itacademy.blackjack.game.controller;

import com.itacademy.blackjack.deck.model.ScoringService;
import com.itacademy.blackjack.game.dto.GameRequest;
import com.itacademy.blackjack.game.dto.GameResponse;
import com.itacademy.blackjack.game.model.Game;
import com.itacademy.blackjack.game.model.exception.MissingIdentifierException;

import com.itacademy.blackjack.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {


    ScoringService scoringService = new ScoringService();

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameResponse> startNewGame(@Valid @RequestBody GameRequest gameRequest) {
        GameService gameService = new GameService(scoringService);
        return gameService.startNewGame(gameRequest.playerId());
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Game> getGameById(@PathVariable UUID id) throws MissingIdentifierException {

        GameService gameService = new GameService(scoringService);
        return gameService.getGameById(id);
    }

/*    @PostMapping("{id}/play")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Game> makeMove( @PathVariable UUID id, @Valid @RequestBody MoveRequest moveRequest) throws MissingIdentifierException {

        return bettingService.makeMove(id, moveRequest);
    }*/

    @DeleteMapping("{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable UUID id) throws MissingIdentifierException {
        GameService gameService = new GameService(scoringService);
        return gameService.deleteById(id);
    }

}
