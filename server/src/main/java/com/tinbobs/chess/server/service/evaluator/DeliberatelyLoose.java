package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class DeliberatelyLoose implements Evaluator {

    private static final Greedy greedy = new Greedy();


    @Override
    public int evaluate(GameState state, Colour perspective) {
        return -greedy.evaluate(state, perspective);
    }

    @Override
    public int staticEvaluate(GameState state, Colour perspective) {
        return -greedy.staticEvaluate(state, perspective);
    }

    @Override
    public @NonNull Move bestMove(GameState state, Colour perspective) {

        Set<Move> legalMoves = state.getLegalMoves();
        Move worstMove = null;
        int worstScore = Integer.MAX_VALUE;

        for (Move move : legalMoves) {

            GameState newState = state.advance(move);
            Set<Move> opponentMoves = newState.getLegalMoves();
            int score;

            //probably won't happen
            if (opponentMoves.isEmpty()) {
                score = greedy.staticEvaluate(newState, perspective);
            }

            else {

                //simulate an opponent move
                int bestOpponentScore = Integer.MIN_VALUE;
                Colour opponentColour = newState.currentTurn().getColour();

                for (Move opponentMove : opponentMoves) {
                    int s = greedy.staticEvaluate(newState.advance(opponentMove), opponentColour);
                    if (s > bestOpponentScore) {
                        bestOpponentScore = s;
                    }
                }
                score = -bestOpponentScore;
            }

            if (score < worstScore) {
                worstScore = score;
                worstMove = move;
            }
        }

        assert(worstMove != null);
        return worstMove;
    }

    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state, Colour perspective) {

        Queue<Move> bestMoveFirst = greedy.getSortedMoves(state, perspective);
        Stack<Move> tempStack = new Stack<>();

        //add each element of bestMoveFirst to the stack
        while (!bestMoveFirst.isEmpty()) {
            tempStack.add(bestMoveFirst.poll());
        }

        //now add the elements of the stack to a new queue
        Queue<Move> worstMoveFirst = new ArrayDeque<>();
        while (!tempStack.isEmpty()) {
            worstMoveFirst.add(tempStack.pop());
        }

        return worstMoveFirst;
    }

    @Override
    public void reset() {}

    @Override
    public void shutdown() {}
}
