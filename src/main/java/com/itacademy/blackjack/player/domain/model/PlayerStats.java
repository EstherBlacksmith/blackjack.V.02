package com.itacademy.blackjack.player.domain.model;

public record PlayerStats(
        int totalGames,
        int wins,
        int losses,
        int pushes,
        double winRate
) {
    public PlayerStats(int wins, int losses, int pushes) {
        this(wins + losses + pushes, wins, losses, pushes, calculateWinRate(wins, losses, pushes));
    }

    private static double calculateWinRate(int wins, int losses, int pushes) {
        int total = wins + losses + pushes;
        if (total == 0) return 0.0;
        return (double) wins / total * 100;
    }
}