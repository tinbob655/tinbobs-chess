package com.tinbobs.chess.server.model;

import com.tinbobs.chess.server.model.state.Move;

public final class IllegalMoveException extends RuntimeException {

    public IllegalMoveException(Move move) {
        super("Illegal move: " + move.from() + " -> " + move.to());
    }
}
