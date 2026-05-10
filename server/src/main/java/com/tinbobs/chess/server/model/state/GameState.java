package com.tinbobs.chess.server.model.state;


import java.util.HashSet;
import java.util.Set;

public record GameState() {

    public Set<Move> getLegalMoves() {
        Set<Move> res = new HashSet<>();

        return res;
    }

    public boolean isGameOver() {
        return false;
    }
}
