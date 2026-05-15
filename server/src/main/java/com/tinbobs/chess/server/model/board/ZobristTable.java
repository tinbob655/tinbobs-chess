package com.tinbobs.chess.server.model.board;

import com.tinbobs.chess.server.model.piece.*;

import java.util.Random;
import com.tinbobs.chess.server.model.piece.*;

import java.util.List;
import java.util.Optional;

public final class ZobristTable {


    private static final long[][] TABLE = new long[64][12];

    static {
        Random rng = new Random(0xDEADBEEFL);
        for (int sq = 0; sq < 64; sq++) {
            for (int p = 0; p < 12; p++) {
                TABLE[sq][p] = rng.nextLong();
            }
        }
    }

    public static long hash(List<Optional<Piece>> grid) {
        long h = 0L;
        for (int sq = 0; sq < 64; sq++) {
            if (grid.get(sq).isPresent()) {
                h ^= TABLE[sq][pieceIndex(grid.get(sq).get())];
            }
        }
        return h;
    }

    private static int pieceIndex(Piece piece) {


        int typeIndex;
        if      (piece instanceof Pawn)   typeIndex = 0;
        else if (piece instanceof Rook)   typeIndex = 1;
        else if (piece instanceof Knight) typeIndex = 2;
        else if (piece instanceof Bishop) typeIndex = 3;
        else if (piece instanceof Queen)  typeIndex = 4;
        else if (piece instanceof King)   typeIndex = 5;
        else throw new IllegalArgumentException("Unknown piece type: " + piece.getClass().getName());

        int colourOffset = piece.getColour() == Colour.WHITE ? 0 : 6;
        return typeIndex + colourOffset;
    }
}
