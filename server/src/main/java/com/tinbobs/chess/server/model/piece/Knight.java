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
}
