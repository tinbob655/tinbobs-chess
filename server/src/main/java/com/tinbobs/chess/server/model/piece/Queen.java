package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.HashSet;
import java.util.Set;

public final class Queen extends Piece {

    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public Queen(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {

        //the queen can move diagonally and in straight lines
        return new HashSet<>(this.traverseAndGenerateMoves(MOVEMENT_DIRECTIONS, startingPos, board));
    }

    @Override
    public int getValue() {
        return 9;
    }

    @Override
    public int[] getPieceTable() {
        return new int[] {
                -20,-10,-10, -5, -5,-10,-10,-20,
                -10,  0,  0,  0,  0,  0,  0,-10,
                -10,  0,  5,  5,  5,  5,  0,-10,
                -5,  0,  5,  5,  5,  5,  0, -5,
                0,  0,  5,  5,  5,  5,  0, -5,
                -10,  5,  5,  5,  5,  5,  0,-10,
                -10,  0,  5,  0,  0,  0,  0,-10,
                -20,-10,-10, -5, -5,-10,-10,-20,
        };
    }
}
