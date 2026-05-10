package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.board.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.springframework.stereotype.Service;
import java.util.Set;


public final class Bot extends Player {

    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    public Move turn(GameState state) {

        //do a random move for now
        Set<Move> validMoves = state.getLegalMoves();
        return validMoves.stream().findAny().orElseThrow();
    }
}
