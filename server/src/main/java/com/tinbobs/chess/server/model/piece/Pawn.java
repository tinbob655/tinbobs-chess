package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.HashSet;
import java.util.Set;

public final class Pawn extends Piece {


    public Pawn(Colour colour) {
        super(colour);
    }

    @Override
    public Set<Move> getLegalMoves(Position startingPos, Board board) {

        Set<Move> res = new HashSet<>();

        //a pawn can move one forward if nothing is in front of it
        int forward = this.getColour() == Colour.WHITE ? 1 : -1;
        Position inFront = startingPos.add(0, forward);
        if (board.getPieceAt(inFront).isEmpty()) {
            res.add(new Move(startingPos, inFront, this.getColour().name()));
        }

        //if we could move forward, and we are in our starting position we might be able to move 2 squares forward
        int pawnStart = this.getColour() == Colour.WHITE ? 2 : 7;
        if (!res.isEmpty() && startingPos.y() == pawnStart) {
            Position twoInFront = inFront.add(0, forward);
            if (board.getPieceAt(twoInFront).isEmpty()) {
                res.add(new Move(startingPos, twoInFront, this.getColour().name()));
            }
        }

        //finally, a pawn can capture another piece if it is diagonal-forward
        int[][] diagonals = new int[][]{{1, forward}, {-1, forward}};
        for (int[] diagonal : diagonals) {
            Position pos = startingPos.add(diagonal[0], diagonal[1]);
            if (!pos.outOfBounds()) {
                board.getPieceAt(pos).ifPresent(piece -> {
                    if (piece.getColour() != this.getColour()) {

                        //capture is allowed
                        res.add(new Move(startingPos, pos, this.getColour().name()));
                    }
                });
            }
        }

        return res;
    }

    @Override
    public int getValue() {
        return 1;
    }

    @Override
    public int[] getPieceTable() {
        return new int[]{
                0,  0,  0,  0,  0,  0,  0,  0,
                50, 50, 50, 50, 50, 50, 50, 50,
                10, 10, 20, 30, 30, 20, 10, 10,
                5,  5, 10, 25, 25, 10,  5,  5,
                0,  0,  0, 20, 20,  0,  0,  0,
                5, -5,-10,  0,  0,-10, -5,  5,
                5, 10, 10,-20,-20, 10, 10,  5,
                0,  0,  0,  0,  0,  0,  0,  0,
        };
    }
}
