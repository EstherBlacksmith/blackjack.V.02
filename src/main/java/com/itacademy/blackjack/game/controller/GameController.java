package com.itacademy.blackjack.game.controller;

import com.itacademy.blackjack.game.dto.GameRequest;
import com.itacademy.blackjack.game.dto.GameResponse;
import com.itacademy.blackjack.game.model.Game;
import com.itacademy.blackjack.game.model.exception.MissingIdentifierException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {


    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameResponse> startNewGame(@Valid @RequestBody GameRequest gameRequest) {
        return game.startGame(gameRequest);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Game> getGame(@PathVariable UUID id) throws MissingIdentifierException {

        return gameService.findById(id);
    }


    @PostMapping("{id}/play")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Game> makeMove( @PathVariable UUID id, @Valid @RequestBody MoveRequest moveRequest) throws MissingIdentifierException {

        return bettingService.makeMove(id, moveRequest);
    }

    @DeleteMapping("{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable UUID id) throws MissingIdentifierException {

        return gameService.deleteById(id);
    }

}
