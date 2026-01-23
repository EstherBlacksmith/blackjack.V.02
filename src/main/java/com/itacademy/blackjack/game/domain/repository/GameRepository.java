package com.itacademy.blackjack.game.domain.repository;

import com.itacademy.blackjack.game.domain.model.Game;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class GameRepository {
    private final Map<UUID, Game> games = new HashMap<>();

    public Game save(Game game) {
        games.put(game.getId(), game);
        return game;
    }

    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(games.get(id));
    }

    public void delete(UUID id) {
        games.remove(id);
    }


}
