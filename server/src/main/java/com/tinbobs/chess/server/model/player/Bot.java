package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.service.evaluator.Evaluator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public final class Bot extends Player {

    @Autowired
    private Evaluator evaluator;


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    @NonNull
    public Move turn(GameState state) {
        System.out.println("Bot is thinking...");
        Move move = this.evaluator.bestMove(state, this.getColour());
        System.out.println("Bot decided on move: " + move);
        return move;
    }
}
