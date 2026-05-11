package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.HashSet;
import java.util.Set;

public final class Rook extends Piece {

    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    public Rook(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {

        //the rook can move in straight lines
        return new HashSet<>(this.traverseAndGenerateMoves(MOVEMENT_DIRECTIONS, startingPos, board));
    }

    @Override
    public int getValue() {
        return 5;
    }
}
