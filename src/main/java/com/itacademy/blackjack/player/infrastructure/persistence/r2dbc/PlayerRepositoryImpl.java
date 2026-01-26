package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import com.itacademy.blackjack.player.application.dto.PlayerStatsResponse;
import com.itacademy.blackjack.player.domain.model.Player;
import io.r2dbc.spi.Readable;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    private final DatabaseClient client;
    private final PlayerMapper mapper;

    public PlayerRepositoryImpl(DatabaseClient client, PlayerMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public Mono<Player> save(Player player) {
        PlayerEntity entity = mapper.toEntity(player);
        return client.sql(
                        "INSERT INTO players (id, name, wins, losses, pushes) VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE name = VALUES(name), wins = VALUES(wins), " +
                                "losses = VALUES(losses), pushes = VALUES(pushes)"
                )
                .bind(0, entity.id())
                .bind(1, entity.name())
                .bind(2, entity.wins())
                .bind(3, entity.losses())
                .bind(4, entity.pushes())
                .then()
                .thenReturn(player);
    }


    @Override
    public Mono<Void> deleteById(UUID id) {
        return client.sql("DELETE FROM players WHERE id = ?")
                .bind(0, id.toString())
                .then();
    }

    @Override
    public Mono<Player> findById(UUID playerId) {
        return client.sql("SELECT * FROM players WHERE id = ?")
                .bind(0, playerId.toString())
                .map((io.r2dbc.spi.Readable row) -> mapRowToPlayer(row))
                .first();
    }

    @Override
    public Mono<Player> findByName(String name) {
        return client.sql("SELECT * FROM players WHERE name = ?")
                .bind(0, name)
                .map((io.r2dbc.spi.Readable row) -> mapRowToPlayer(row))
                .first();
    }

    private Player mapRowToPlayer(Readable row) {
        return Player.fromDatabase(
                UUID.fromString(row.get("id", String.class)),
                row.get("name", String.class),
                row.get("wins", Integer.class),
                row.get("losses", Integer.class),
                row.get("pushes", Integer.class)
        );
    }

    @Override
    public Mono<Player> updateStats(UUID playerId, int wins, int losses, int pushes) {
        System.out.println("[DEBUG] PlayerRepositoryImpl.updateStats called for playerId: " + playerId + ", wins: " + wins + ", losses: " + losses + ", pushes: " + pushes);
        return client.sql(
                        "UPDATE players SET wins = ?, losses = ?, pushes = ? WHERE id = ?"
                )
                .bind(0, wins)
                .bind(1, losses)
                .bind(2, pushes)
                .bind(3, playerId.toString())
                .then()
                .doOnSuccess(v -> System.out.println("[DEBUG] Player stats UPDATE executed successfully for playerId: " + playerId))
                .doOnError(e -> System.err.println("[ERROR] Player stats UPDATE failed for playerId: " + playerId + ", error: " + e.getMessage()))
                .thenReturn(Player.fromDatabase(playerId, null, wins, losses, pushes));
    }


}

