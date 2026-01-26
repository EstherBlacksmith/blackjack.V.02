package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

/**
 * SQL constants for Player repository operations.
 * Centralized SQL queries for maintainability.
 */

public final class PlayerSqlConstants {

    // INSERT
    public static final String INSERT_PLAYER =
            "INSERT INTO players (id, name, wins, losses, pushes) VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), wins = VALUES(wins), " +
                    "losses = VALUES(losses), pushes = VALUES(pushes)";
    // SELECT
    public static final String SELECT_PLAYER_BY_ID =
            "SELECT * FROM players WHERE id = ?";
    public static final String SELECT_PLAYER_BY_NAME =
            "SELECT * FROM players WHERE name = ?";
    public static final String SELECT_ALL_PLAYERS_RANKING =
            "SELECT name, MAX(id) as id, MAX(name) as name, " +
                    "COALESCE(SUM(wins), 0) as wins, COALESCE(SUM(losses), 0) as losses, " +
                    "COALESCE(SUM(pushes), 0) as pushes FROM players " +
                    "GROUP BY name ORDER BY wins DESC";
    // UPDATE
    public static final String UPDATE_PLAYER_STATS =
            "UPDATE players SET wins = ?, losses = ?, pushes = ? WHERE id = ?";
    // DELETE
    public static final String DELETE_PLAYER_BY_ID =
            "DELETE FROM players WHERE id = ?";


}

