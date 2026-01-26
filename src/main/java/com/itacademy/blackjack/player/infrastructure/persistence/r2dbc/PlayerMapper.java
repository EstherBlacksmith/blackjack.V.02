package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import com.itacademy.blackjack.player.domain.model.Player;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlayerMapper {

    // Player (domain) → PlayerEntity (MySQL)
    public PlayerEntity toEntity(Player player) {
        return new PlayerEntity(
                player.getId().toString(),
                player.getName(),
                player.getWins(),
                player.getLosses(),
                player.getPushes()
        );
    }

    // PlayerEntity (MySQL) → Player (domain)
    public Player toDomain(PlayerEntity entity) {
        return Player.fromDatabase(
                UUID.fromString(entity.id()),
                entity.name(),
                entity.wins(),
                entity.losses(),
                entity.pushes()
        );
    }

    public Player fromRow(Row row) {
        return Player.fromDatabase(
                UUID.fromString(row.get("id", String.class)),
                row.get("name", String.class),
                row.get("wins", Integer.class),
                row.get("losses", Integer.class),
                row.get("pushes", Integer.class)
        );
    }

}
