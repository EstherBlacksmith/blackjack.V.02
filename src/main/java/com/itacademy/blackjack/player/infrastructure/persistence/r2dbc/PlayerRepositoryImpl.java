package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;


import com.itacademy.blackjack.player.domain.model.Player;
import com.itacademy.blackjack.player.domain.repository.PlayerRepository;
import io.r2dbc.spi.Readable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import static com.itacademy.blackjack.player.infrastructure.persistence.r2dbc.PlayerSqlConstants.*;

@Slf4j
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
        return client.sql(INSERT_PLAYER)
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
        return client.sql(DELETE_PLAYER_BY_ID)
                .bind(0, id.toString())
                .then();
    }

    @Override
    public Mono<Player> findById(UUID playerId) {
        return client.sql(SELECT_PLAYER_BY_ID)
                .bind(0, playerId.toString())
                .map((io.r2dbc.spi.Readable row) -> mapRowToPlayer(row))
                .first();
    }

    @Override
    public Mono<Player> findByName(String name) {
        return client.sql(SELECT_PLAYER_BY_NAME)
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
        log.debug("updateStats called for playerId: {}, wins: {}, losses: {}, pushes: {}",
                playerId, wins, losses, pushes);

        return client.sql(UPDATE_PLAYER_STATS)
                .bind(0, wins)
                .bind(1, losses)
                .bind(2, pushes)
                .bind(3, playerId.toString())
                .then()
                .doOnSuccess(v -> log.debug("Player stats UPDATE executed successfully for playerId: {}", playerId))
                .doOnError(e -> log.error("Player stats UPDATE failed for playerId: {}", playerId, e))
                .thenReturn(Player.fromDatabase(playerId, null, wins, losses, pushes));

    }

    @Override
    public Flux<Player> findAllByOrderByWinsDesc() {

        return client.sql(SELECT_ALL_PLAYERS_RANKING)
                .map((io.r2dbc.spi.Readable row) -> {
                    String idStr = row.get("id", String.class);
                    String name = row.get("name", String.class);
                    int wins = row.get("wins") != null ? row.get("wins", Integer.class) : 0;
                    int losses = row.get("losses") != null ? row.get("losses", Integer.class) : 0;
                    int pushes = row.get("pushes") != null ? row.get("pushes", Integer.class) : 0;

                    UUID id;
                    if (idStr != null && !idStr.isEmpty()) {
                        id = UUID.fromString(idStr);
                    } else {
                        id = UUID.nameUUIDFromBytes(name.toLowerCase().getBytes());
                    }

                    return Player.fromDatabase(id, name, wins, losses, pushes);
                })
                .all();
    }


}

