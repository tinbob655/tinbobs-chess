package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;


public final class Bot extends Player {

    @Autowired
    private PositionEvaluator evaluator;

    private static final int MINIMAX_DEPTH = 4;


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    @NonNull
    public Move turn(GameState state) {

        System.out.println("Bot is thinking...");

        //get all possible move and sort them by suspected best
        Queue<Move> sortedMoves = this.evaluator.getSortedMoves(state);

        if (sortedMoves.isEmpty()) {
            throw new IllegalStateException("No possible moves");
        }

        //do mini-max on each move
        Move bestMove = sortedMoves.peek();
        int bestScore = Integer.MIN_VALUE;
        while (!sortedMoves.isEmpty()) {

            Move move = sortedMoves.poll();

            //pretend we did the move
            GameState newState = state.advance(move);
            int moveScore = this.evaluator.minimax(newState, MINIMAX_DEPTH -1, Integer.MIN_VALUE, Integer.MAX_VALUE, this.getColour());

            //keep track of the best move
            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestMove = move;
            }
        }

        System.out.println("Bot decided on a move: " + bestMove.toString());

        return bestMove;
    }
}
