package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.HashSet;
import java.util.Set;

public final class Bishop extends Piece {

    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public Bishop(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {
        return new HashSet<>(this.traverseAndGenerateMoves(MOVEMENT_DIRECTIONS, startingPos, board));
    }

    @Override
    public int getValue() {
        return 3;
    }
}
