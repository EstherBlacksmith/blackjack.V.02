package com.itacademy.blackjack.game.controller;

import com.itacademy.blackjack.game.model.Game;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "games")
public class GameController {

   /* @GetMapping
    public Flux<Game> getAllGames(){

    }*/

}
