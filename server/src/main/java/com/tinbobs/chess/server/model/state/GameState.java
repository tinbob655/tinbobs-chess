package com.tinbobs.chess.server.model.state;


import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.player.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record GameState(Board board, Player currentTurn) {

    public Set<Move> getLegalMoves() {
        Set<Move> res = new HashSet<>();

        //add all pieces legal moves
        board.getGrid().parallelStream()
                .filter(Optional::isPresent)
                .forEach(piece -> {
                    res.addAll(piece.get().getLegalMoves());
                });

        return res;
    }

    public boolean isGameOver() {
        return false;
    }
}
