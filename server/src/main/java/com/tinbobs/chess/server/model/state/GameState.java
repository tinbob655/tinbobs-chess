package com.tinbobs.chess.server.model.state;


import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.player.Player;

import java.util.HashSet;
import java.util.Set;

public record GameState(Board board, Player currentTurn) {

    public Set<Move> getLegalMoves() {
        Set<Move> res = new HashSet<>();

        //add all pieces legal moves
        for (int i = 0; i < 64; i++) {

            int xInt = i % 8;
            int yInt = i / 8;
            char x = (char) ('a' + xInt);
            Position pos = new Position(x, yInt);

            board.getPieceAt(pos).ifPresent(piece -> {
                res.addAll(piece.getLegalMoves(pos, board));
            });
        }

        return res;
    }

    public boolean isGameOver() {
        return false;
    }
}
