package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.Set;

public final class Knight extends Piece {


    public Knight(Colour colour, Position pos) {
        super(colour, pos);
    }

    @Override
    public Set<Move> getLegalMoves() {
        return Set.of();
    }

    @Override
    public int getValue() {
        return 3;
    }
}
