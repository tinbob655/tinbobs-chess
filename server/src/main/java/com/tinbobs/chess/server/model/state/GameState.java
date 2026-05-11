package com.tinbobs.chess.server.model.state;


import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.player.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class GameState {

    private final Board board;
    private final Player currentTurn;
    private final Set<Move> cachedLegalMoves = new HashSet<>();

    public GameState(Board board, Player currentTurn) {
        this.board = board;
        this.currentTurn = currentTurn;
    }

    //getters
    public Board board() {
        return this.board;
    }
    public Player currentTurn() {
        return this.currentTurn;
    }

    public Set<Move> getLegalMoves() {

        //only calculate moves once to save computation
        if (!this.cachedLegalMoves.isEmpty()) {
            return this.cachedLegalMoves;
        }
        
        Set<Move> res = new HashSet<>();

        //add all pieces legal moves
        for (int i = 0; i < 64; i++) {

            int xInt = i % 8;
            int yInt = i / 8;
            char x = (char) ('a' + xInt);
            Position pos = new Position(x, yInt);

            board.getPieceAt(pos).ifPresent(piece -> {

                if (piece.getColour() == currentTurn.getColour()) {
                    res.addAll(piece.getLegalMoves(pos, board));
                }
            });
        }

        return res;
    }

    public boolean isGameOver() {
        return false;
    }


    //equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState s)) {
            return false;
        }

        return (
                (s.board().equals(this.board))
                && (s.currentTurn().equals(this.currentTurn))
                );
    }
    public int hashCode() {
        return Objects.hash(this.board, this.currentTurn);
    }
}
