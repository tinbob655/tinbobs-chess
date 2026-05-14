package com.tinbobs.chess.server.service;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class PositionEvaluator {

    //gives a state a score from a player's perspective
    public int evaluate(GameState state, Colour perspective) {

        int res = 0;
        List<Optional<Piece>> grid = state.board().getGrid();

        for (int i = 0; i < 64; i++) {

            if (grid.get(i).isEmpty()) continue;
            Piece piece = grid.get(i).get();

            //our piece: add. Opponent's piece: subtract
            int sign = piece.getColour() == perspective ? 1 : -1;
            int material = piece.getValue() * 100;

            //tables are written from white's POV so need to flip if we are black
            boolean pieceIsWhite = piece.getColour() == Colour.WHITE;
            boolean perspectiveIsWhite = perspective == Colour.WHITE;
            int tableIndex = (pieceIsWhite == perspectiveIsWhite) ? i : mirror(i);
            int positional = piece.getPieceTable()[tableIndex];

            res += sign * (material + positional);
        }

        //a piece with more available moves is in a better position
        int mobilityBonus = state.currentTurn().getColour() == perspective ? 5 : -5;
        res += mobilityBonus * state.getLegalMoves().size();

        return res;
    }

    //helper to flip a table
    private int mirror(int index) {
        int col = index % 8;
        int row = index / 8;
        return (7 - row) * 8 + col;
    }
}
