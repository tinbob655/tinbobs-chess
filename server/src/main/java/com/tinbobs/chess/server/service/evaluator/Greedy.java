package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.Status;
import org.jspecify.annotations.NonNull;

import java.util.*;


public final class Greedy implements Evaluator {


    @Override
    public int evaluate(GameState state, Colour perspective) {

        //greedy algorithm will only ever look one move ahead => just use the static evaluate method
        return this.staticEvaluate(state, perspective);
    }

    @Override
    public int staticEvaluate(GameState state, Colour perspective) {

        //checkmate is the goal
        if (state.getStatus() == Status.CHECKMATE) {
            return (state.currentTurn().getColour() == perspective ? Integer.MAX_VALUE : Integer.MIN_VALUE) / 2;
        }

        int res = 0;

        //reward / punish for existing pieces
        res += state.board().getGrid().parallelStream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToInt(piece -> piece.getColour() == perspective ? piece.getValue() : -piece.getValue())
                .sum();

        return res;
    }

    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state) {

        //compare moves based on static evaluation
        Comparator<Move> moveComparator = Comparator.comparingInt((Move move) -> {

            //pretend we did the move
            Colour perspective = state.currentTurn().getColour();
            GameState newState = state.advance(move);
            return this.staticEvaluate(newState, perspective);
        }).reversed();

        Queue<Move> sortedMoves = new PriorityQueue<>(moveComparator);
        Set<Move> legalMoves = state.getLegalMoves();
        sortedMoves.addAll(legalMoves);
        return sortedMoves;
    }

    @Override
    public void reset() {
        //don't need to do anything
    }

    @Override
    public void shutdown() {}

    @Override
    @NonNull
    public Move bestMove(GameState state, Colour perspective) {

        Set<Move> legalMoves = state.getLegalMoves();
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            int score = this.staticEvaluate(state.advance(move), perspective);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        assert(bestMove != null);
        return bestMove;
    }
}
