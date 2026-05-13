package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;
import java.util.Set;

public final class Knight extends Piece {

    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{2, 1}, {-2, 1}, {2, -1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};


    public Knight(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {
        return this.spiralAndGenerateMoves(MOVEMENT_DIRECTIONS, startingPos, board);
    }

    @Override
    public int getValue() {
        return 3;
    }

    @Override
    public int[] getPieceTable() {
        return new int[] {
                -50,-40,-30,-30,-30,-30,-40,-50,
                -40,-20,  0,  0,  0,  0,-20,-40,
                -30,  0, 10, 15, 15, 10,  0,-30,
                -30,  5, 15, 20, 20, 15,  5,-30,
                -30,  0, 15, 20, 20, 15,  0,-30,
                -30,  5, 10, 15, 15, 10,  5,-30,
                -40,-20,  0,  5,  5,  0,-20,-40,
                -50,-40,-30,-30,-30,-30,-40,-50,
        };
    }
}
