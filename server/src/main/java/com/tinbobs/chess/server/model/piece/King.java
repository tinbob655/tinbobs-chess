package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;
import java.util.Set;

public final class King extends Piece {

    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public King(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {
        return this.spiralAndGenerateMoves(MOVEMENT_DIRECTIONS, startingPos, board);
    }

    @Override
    public int getValue() {
        return Integer.MAX_VALUE / 2;
    }

    @Override
    public int[] getPieceTable() {
        return new int[] {
                -30,-40,-40,-50,-50,-40,-40,-30,
                -30,-40,-40,-50,-50,-40,-40,-30,
                -30,-40,-40,-50,-50,-40,-40,-30,
                -30,-40,-40,-50,-50,-40,-40,-30,
                -20,-30,-30,-40,-40,-30,-30,-20,
                -10,-20,-20,-20,-20,-20,-20,-10,
                20, 20,  0,  0,  0,  0, 20, 20,
                20, 30, 10,  0,  0, 10, 30, 20,
        };
    }
}
