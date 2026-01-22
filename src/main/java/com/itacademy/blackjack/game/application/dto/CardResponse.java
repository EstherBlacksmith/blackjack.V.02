package com.itacademy.blackjack.game.application.dto;

public record CardResponse( String rank,    // "TEN", "JACK", "QUEEN", "KING", "ACE"
                            String suit,    // "HEARTS", "SPADES", "DIAMONDS", "CLUBS"
                            int value ){ // 10, 10, 10, 10, 11 (or 1)
}
