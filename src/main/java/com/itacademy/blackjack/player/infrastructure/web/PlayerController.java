package com.itacademy.blackjack.player.infrastructure.web;

import com.itacademy.blackjack.player.application.PlayerService;
import com.itacademy.blackjack.player.application.dto.CreatePlayerRequest;
import com.itacademy.blackjack.player.application.dto.PlayerProfileResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.game.domain.model.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PlayerProfileResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return playerService.createPlayer(request.name())
                .map(this::toProfileResponse);
    }

    @GetMapping("/{id}")
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
}
